package com.ofcoder.klein.consensus.facade.quorum;

import java.util.HashSet;
import java.util.Set;

import com.ofcoder.klein.rpc.facade.Endpoint;
import com.ofcoder.klein.rpc.facade.util.RpcUtil;
import junit.framework.TestCase;

public class JoinConsensusQuorumTest extends TestCase {

    public void testRefuse() {
        Set<Endpoint> effect = new HashSet<>();
        effect.add(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        effect.add(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        effect.add(RpcUtil.parseEndpoint("3:127.0.0.1:1220"));
        Set<Endpoint> last = new HashSet<>();
        last.add(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        last.add(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        last.add(RpcUtil.parseEndpoint("3:127.0.0.1:1220"));
        last.add(RpcUtil.parseEndpoint("4:127.0.0.1:1221"));
        JoinConsensusQuorum quorum = new JoinConsensusQuorum(effect, last, 2 , 3);
        SingleQuorum oldQuorum = (SingleQuorum) quorum.getOldQuorum();
        SingleQuorum newQuorum = (SingleQuorum) quorum.getNewQuorum();

        // granting
        quorum.refuse(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        assertEquals(oldQuorum.getFailedMembers().size(), 1);
        assertTrue(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertFalse(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertEquals(newQuorum.getFailedMembers().size(), 1);
        assertTrue(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertFalse(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.GRANTING);

        // not exists
        quorum.refuse(RpcUtil.parseEndpoint("-1:127.0.0.1:1218"));
        assertEquals(oldQuorum.getFailedMembers().size(), 1);
        assertFalse(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("-1:127.0.0.1:1218")));
        assertEquals(newQuorum.getFailedMembers().size(), 1);
        assertFalse(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("-1:127.0.0.1:1218")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.GRANTING);

        // refuse
        quorum.refuse(RpcUtil.parseEndpoint("4:127.0.0.1:1221"));
        assertEquals(oldQuorum.getFailedMembers().size(), 1);
        assertFalse(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertFalse(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertEquals(newQuorum.getFailedMembers().size(), 2);
        assertTrue(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertFalse(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.REFUSE);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.REFUSE);

        // refuse
        quorum.refuse(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        assertEquals(oldQuorum.getFailedMembers().size(), 2);
        assertTrue(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertFalse(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertEquals(newQuorum.getFailedMembers().size(), 3);
        assertTrue(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertFalse(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.REFUSE);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.REFUSE);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.REFUSE);
    }

    public void testGrant() {
        Set<Endpoint> effect = new HashSet<>();
        effect.add(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        effect.add(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        effect.add(RpcUtil.parseEndpoint("3:127.0.0.1:1220"));
        Set<Endpoint> last = new HashSet<>();
        last.add(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        last.add(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        last.add(RpcUtil.parseEndpoint("3:127.0.0.1:1220"));
        last.add(RpcUtil.parseEndpoint("4:127.0.0.1:1221"));
        JoinConsensusQuorum quorum = new JoinConsensusQuorum(effect, last, 2 , 3);
        SingleQuorum oldQuorum = (SingleQuorum) quorum.getOldQuorum();
        SingleQuorum newQuorum = (SingleQuorum) quorum.getNewQuorum();

        // granting
        quorum.grant(RpcUtil.parseEndpoint("1:127.0.0.1:1218"));
        assertEquals(oldQuorum.getGrantedMembers().size(), 1);
        assertTrue(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertFalse(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertEquals(newQuorum.getGrantedMembers().size(), 1);
        assertTrue(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertFalse(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("1:127.0.0.1:1218")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.GRANTING);

        // not exists
        quorum.grant(RpcUtil.parseEndpoint("-1:127.0.0.1:1218"));
        assertEquals(oldQuorum.getGrantedMembers().size(), 1);
        assertFalse(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("-1:127.0.0.1:1218")));
        assertEquals(newQuorum.getGrantedMembers().size(), 1);
        assertFalse(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("-1:127.0.0.1:1218")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.GRANTING);

        // granting
        quorum.grant(RpcUtil.parseEndpoint("4:127.0.0.1:1221"));
        assertEquals(oldQuorum.getGrantedMembers().size(), 1);
        assertFalse(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertFalse(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertEquals(newQuorum.getGrantedMembers().size(), 2);
        assertTrue(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertFalse(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("4:127.0.0.1:1221")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.GRANTING);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.GRANTING);

        // pass
        quorum.grant(RpcUtil.parseEndpoint("2:127.0.0.1:1219"));
        assertEquals(oldQuorum.getGrantedMembers().size(), 2);
        assertTrue(oldQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertFalse(oldQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertEquals(newQuorum.getGrantedMembers().size(), 3);
        assertTrue(newQuorum.getGrantedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertFalse(newQuorum.getFailedMembers().contains(RpcUtil.parseEndpoint("2:127.0.0.1:1219")));
        assertEquals(oldQuorum.isGranted(), Quorum.GrantResult.PASS);
        assertEquals(newQuorum.isGranted(), Quorum.GrantResult.PASS);
        assertEquals(quorum.isGranted(), Quorum.GrantResult.PASS);
    }

    public void testIsGranted() {
    }
}