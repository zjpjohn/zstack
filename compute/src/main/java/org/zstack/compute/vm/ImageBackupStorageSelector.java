package org.zstack.compute.vm;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.header.storage.backup.BackupStorageStatus;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by frank on 10/17/2015.
 */
@Configurable(preConstruction = true, autowire = Autowire.BY_TYPE)
public class ImageBackupStorageSelector {
    @Autowired
    private DatabaseFacade dbf;

    private String imageUuid;
    private String zoneUuid;
    private boolean checkStatus = true;

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
    }

    public String getZoneUuid() {
        return zoneUuid;
    }

    public void setZoneUuid(String zoneUuid) {
        this.zoneUuid = zoneUuid;
    }

    public String select() {
        assert imageUuid != null : "imageUuid cannot be null";
        assert zoneUuid != null : "zoneUuid cannot be null";

        TypedQuery<String> q;
        if (checkStatus) {
            String sql = "select bs.uuid from BackupStorageVO bs, BackupStorageZoneRefVO bsRef, ImageBackupStorageRefVO iref where" +
                    " bs.uuid = bsRef.backupStorageUuid and bsRef.zoneUuid = :zoneUuid and iref.backupStorageUuid = bs.uuid and" +
                    " bs.status = :bsStatus and iref.imageUuid = :imageUuid";
            q = dbf.getEntityManager().createQuery(sql, String.class);
            q.setParameter("zoneUuid", zoneUuid);
            q.setParameter("bsStatus", BackupStorageStatus.Connected);
            q.setParameter("imageUuid", imageUuid);
        } else {
            String sql = "select bs.uuid from BackupStorageVO bs, BackupStorageZoneRefVO bsRef, ImageBackupStorageRefVO iref where" +
                    " bs.uuid = bsRef.backupStorageUuid and bsRef.zoneUuid = :zoneUuid and iref.backupStorageUuid = bs.uuid and" +
                    " iref.imageUuid = :imageUuid";
            q = dbf.getEntityManager().createQuery(sql, String.class);
            q.setParameter("zoneUuid", zoneUuid);
            q.setParameter("imageUuid", imageUuid);
        }

        List<String> bsUuids = q.getResultList();
        if (bsUuids.isEmpty()) {
            return null;
        }

        return bsUuids.get(0);
    }
}