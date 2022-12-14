package com.ofcoder.klein;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ofcoder.klein.common.exception.StartupException;
import com.ofcoder.klein.consensus.facade.Consensus;
import com.ofcoder.klein.consensus.facade.ConsensusEngine;
import com.ofcoder.klein.core.cache.KleinCache;
import com.ofcoder.klein.core.cache.KleinCacheImpl;
import com.ofcoder.klein.core.config.KleinProp;
import com.ofcoder.klein.core.lock.KleinLock;
import com.ofcoder.klein.core.lock.KleinLockImpl;
import com.ofcoder.klein.rpc.facade.RpcEngine;
import com.ofcoder.klein.spi.ExtensionLoader;
import com.ofcoder.klein.storage.facade.StorageEngine;

/**
 * @author far.liu
 */
public class Klein {
    private static final Logger LOG = LoggerFactory.getLogger(Klein.class);
    private static volatile AtomicBoolean started = new AtomicBoolean(false);
    private KleinCache cache;
    private KleinLock lock;

    public void awaitInit() {
        CountDownLatch latch = new CountDownLatch(1);
        Consensus consensus = ExtensionLoader.getExtensionLoader(Consensus.class).getJoin();
        consensus.setListener(() -> {
            LOG.info("=====================klein prepared======================");
            latch.countDown();
        });
        try {
            boolean await = latch.await(15000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

        }
    }

    private void startup() {
        if (started.get()) {
            throw new StartupException("klein engine has started.");
        }
        if (!started.compareAndSet(false, true)) {
            LOG.warn("klein engine is starting.");
            return;
        }
        LOG.info("starting klein...");
        KleinProp prop = KleinProp.loadIfPresent();

        RpcEngine.startup(prop.getRpc(), prop.getRpcProp());
        StorageEngine.getInstance().startup(prop.getStorage(), prop.getStorageProp());
        ConsensusEngine.startup(prop.getConsensus(), prop.getConsensusProp());

        this.cache = new KleinCacheImpl(prop.getCacheProp());
        this.lock = new KleinLockImpl();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("*** shutting down Klein since JVM is shutting down");
            StorageEngine.getInstance().shutdown();
            ConsensusEngine.shutdown();
            RpcEngine.shutdown();
            LOG.info("*** Klein shut down");
        }));
    }

    public KleinCache getCache() {
        return cache;
    }

    public KleinLock getLock() {
        return lock;
    }

    private Klein() {
        startup();
    }

    public static Klein getInstance() {
        return KleinHolder.INSTANCE;
    }

    private static class KleinHolder {
        private static final Klein INSTANCE = new Klein();
    }

}
