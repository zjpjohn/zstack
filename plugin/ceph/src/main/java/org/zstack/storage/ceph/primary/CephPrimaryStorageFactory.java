package org.zstack.storage.ceph.primary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.zstack.core.Platform;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.header.storage.primary.*;
import org.zstack.storage.ceph.CephCapacityUpdateExtensionPoint;
import org.zstack.storage.ceph.CephConstants;
import org.zstack.storage.ceph.MonStatus;
import org.zstack.storage.ceph.MonUri;

import javax.persistence.TypedQuery;

/**
 * Created by frank on 7/28/2015.
 */
public class CephPrimaryStorageFactory implements PrimaryStorageFactory, CephCapacityUpdateExtensionPoint {
    public static final PrimaryStorageType type = new PrimaryStorageType(CephConstants.CEPH_PRIMARY_STORAGE_TYPE);

    @Autowired
    private DatabaseFacade dbf;

    @Override
    public PrimaryStorageType getPrimaryStorageType() {
        return type;
    }

    @Override
    @Transactional
    public PrimaryStorageInventory createPrimaryStorage(PrimaryStorageVO vo, APIAddPrimaryStorageMsg msg) {
        APIAddCephPrimaryStorageMsg cmsg = (APIAddCephPrimaryStorageMsg) msg;

        CephPrimaryStorageVO cvo = new CephPrimaryStorageVO(vo);
        cvo.setType(CephConstants.CEPH_PRIMARY_STORAGE_TYPE);

        dbf.getEntityManager().persist(cvo);

        for (String url : cmsg.getMonUrls()) {
            CephPrimaryStorageMonVO mvo = new CephPrimaryStorageMonVO();
            MonUri uri = new MonUri(url);
            mvo.setUuid(Platform.getUuid());
            mvo.setStatus(MonStatus.Connecting);
            mvo.setHostname(uri.getHostname());
            mvo.setSshUsername(uri.getSshUsername());
            mvo.setSshPassword(uri.getSshPassword());
            mvo.setPrimaryStorageUuid(cvo.getUuid());
            dbf.getEntityManager().persist(mvo);
        }

        return PrimaryStorageInventory.valueOf(cvo);
    }

    @Override
    public PrimaryStorage getPrimaryStorage(PrimaryStorageVO vo) {
        CephPrimaryStorageVO cvo = dbf.findByUuid(vo.getUuid(), CephPrimaryStorageVO.class);
        return new CephPrimaryStorageBase(cvo);
    }

    @Override
    public PrimaryStorageInventory getInventory(String uuid) {
        return CephPrimaryStorageInventory.valueOf(dbf.findByUuid(uuid, CephPrimaryStorageVO.class));
    }

    @Override
    @Transactional
    public void update(String fsid, long total, long avail) {
        String sql = "select cap from PrimaryStorageCapacityVO cap, CephPrimaryStorageVO pri where pri.uuid = cap.uuid and pri.fsid = :fsid";
        TypedQuery<PrimaryStorageCapacityVO> q = dbf.getEntityManager().createQuery(sql, PrimaryStorageCapacityVO.class);
        q.setParameter("fsid", fsid);
        try {
            PrimaryStorageCapacityVO cap = q.getSingleResult();
            cap.setTotalCapacity(total);
            cap.setAvailableCapacity(avail);
            cap.setTotalPhysicalCapacity(total);
            cap.setAvailableCapacity(avail);
            dbf.getEntityManager().merge(cap);
        } catch (EmptyResultDataAccessException e) {
            return;
        }
    }
}