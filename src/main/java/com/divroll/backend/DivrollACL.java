package com.divroll.backend;

import java.util.LinkedList;
import java.util.List;

public class DivrollACL {

  private List<String> aclRead;
  private List<String> aclWrite;
  private Boolean publicRead;
  private Boolean publicWrite;

  public DivrollACL() {}

  public DivrollACL(List<String> aclRead, List<String> aclWrite) {
    setAclRead(aclRead);
    setAclWrite(aclWrite);
  }

  public static DivrollACL build() {
    DivrollACL divrollACL = new DivrollACL();
    return divrollACL;
  }

  public static DivrollACL buildPublicReadWrite() {
    DivrollACL divrollALC = new DivrollACL();
    divrollALC.setPublicWrite(true);
    divrollALC.setPublicRead(true);
    return divrollALC;
  }

  public static DivrollACL buildMasterKeyOnly() {
    DivrollACL divrollALC = new DivrollACL();
    divrollALC.setPublicWrite(false);
    divrollALC.setPublicRead(false);
    divrollALC.setAclRead(null);
    divrollALC.setAclWrite(null);
    return divrollALC;
  }

  public static DivrollACL buildPublicReadMasterKeyWrite() {
    DivrollACL divrollALC = new DivrollACL();
    divrollALC.setPublicWrite(false);
    divrollALC.setPublicRead(true);
    divrollALC.setAclRead(null);
    divrollALC.setAclWrite(null);
    return divrollALC;
  }

  public List<String> getAclRead() {
    if (aclRead == null) {
      aclRead = new LinkedList<String>();
    }
    return aclRead;
  }

  public void setAclRead(List<String> aclRead) {
    this.aclRead = aclRead;
  }

  public List<String> getAclWrite() {
    if (aclWrite == null) {
      aclWrite = new LinkedList<String>();
    }
    return aclWrite;
  }

  public void setAclWrite(List<String> aclWrite) {
    this.aclWrite = aclWrite;
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
