package com.divroll.domino;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DominoACL {

    private List<String> aclRead;
    private List<String> aclWrite;
    private Boolean publicRead;
    private Boolean publicWrite;

    public DominoACL() {}

    public DominoACL(List<String> aclRead, List<String> aclWrite) {
        setAclRead(aclRead);
        setAclWrite(aclWrite);
    }

    public List<String> getAclRead() {
        if(aclRead == null) {
            aclRead = new LinkedList<String>();
        }
        return aclRead;
    }

    public void setAclRead(List<String> aclRead) {
        this.aclRead = aclRead;
    }

    public List<String> getAclWrite() {
        if(aclWrite == null) {
            aclWrite = new LinkedList<String>();
        }
        return aclWrite;
    }

    public void setAclWrite(List<String> aclWrite) {
        this.aclWrite = aclWrite;
    }

    public static DominoACL build() {
        DominoACL dominoACL = new DominoACL();
        return dominoACL;
    }

    public static DominoACL buildPublicReadWrite() {
        DominoACL dominoALC = new DominoACL();
        dominoALC.setPublicWrite(true);
        dominoALC.setPublicRead(true);
        return dominoALC;
    }

    public static DominoACL buildMasterKeyOnly() {
        DominoACL dominoALC = new DominoACL();
        dominoALC.setPublicWrite(false);
        dominoALC.setPublicRead(false);
        dominoALC.setAclRead(null);
        dominoALC.setAclWrite(null);
        return dominoALC;
    }

    public static DominoACL buildPublicReadMasterKeyWrite() {
        DominoACL dominoALC = new DominoACL();
        dominoALC.setPublicWrite(false);
        dominoALC.setPublicRead(true);
        dominoALC.setAclRead(null);
        dominoALC.setAclWrite(null);
        return dominoALC;
    }

    public Boolean getPublicRead() {
        return publicRead;
    }

    public void setPublicRead(Boolean publicRead) {
        this.publicRead = publicRead;
    }

    public Boolean getPublicWrite() {
        return publicWrite;
    }

    public void setPublicWrite(Boolean publicWrite) {
        this.publicWrite = publicWrite;
    }
}
