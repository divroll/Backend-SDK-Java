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

public class DivrollLink {
	private String linkName;
	private List<DivrollEntityStub> entities;

	private DivrollLink() {
	}

	public DivrollLink(String linkName, List<DivrollEntityStub> entities) {
		setLinkName(linkName);
		setEntities(entities);
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public List<DivrollEntityStub> getEntities() {
		if (entities == null) {
			entities = new LinkedList<>();
		}
		return entities;
	}

	public void setEntities(List<DivrollEntityStub> entities) {
		this.entities = entities;
	}
}
