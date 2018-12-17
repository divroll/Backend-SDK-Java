/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.sdk;

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
