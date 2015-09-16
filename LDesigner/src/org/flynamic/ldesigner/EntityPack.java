package org.flynamic.ldesigner;

import java.util.LinkedList;

public class EntityPack {
	private LinkedList<? extends DesignerEntity> entities = new LinkedList<>();
	private Acceptor acceptor;
	
	public EntityPack(LinkedList<? extends DesignerEntity> entities) {
		this.entities = entities;
	}
	
	public EntityPack(LinkedList<? extends DesignerEntity> entities, Acceptor acceptor) {
		this.entities = entities;
		this.acceptor = acceptor;
	}


	public LinkedList<? extends DesignerEntity> getEntities() {
		return entities;
	}

	public void setEntities(LinkedList<? extends DesignerEntity> entities) {
		this.entities = entities;
	}
	
	/**
	 * @return the acceptor
	 */
	public Acceptor getAcceptor() {
		return acceptor;
	}


	/**
	 * @param acceptor the acceptor to set
	 */
	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}

	public static interface Acceptor {
		public boolean accepts(DesignerEntity entity);
	}
}
