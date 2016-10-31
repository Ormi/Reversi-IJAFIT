/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

/**
 * trieda implementujuca disk 
 *
 */
public class Disk {

	protected boolean isWhite;
        protected boolean isInit;
        protected boolean isFrozen;

	public Disk(boolean isWhite,boolean isInit) {

		this.isWhite=isWhite;
                this.isInit=isInit;
                this.isFrozen=false;
	}

        /**
         * zmeni farbu disku, pouziva sa pri tahoch
         */
	public void turn() {

		if (isWhite == true) {
			isWhite=false;
		} else {
			isWhite=true;
		}
	}

        /**
         * vrati farbu disku
         * @return true ak je biely, false ak je cierny
         */
	public boolean isWhite() {
		if (isWhite == true) {
			return true;
		} else {
			return false;
		}
	}
        
        /**
         * vrati ci ide o disk vlozeny pri inicializacii hry
         * @return true ak ano
         */
        public boolean isInit() {
		if (isInit == true) {
			return true;
		} else {
			return false;
		}
	}
        
        /**
         * zmrazi dany disk
         */
        public void freeze() {
            this.isFrozen=true;
        }
        
        /**
         * rozmrazi dany disk
         */
        public void unfreeze() {
            this.isFrozen=false;
        }
        
        /**
         * vrati ci je dany disk zmrazeny
         * @return true ak je zmrazeny, inak false
         */
        public boolean isFrozen() {
            return this.isFrozen;
        }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.isWhite ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Disk other = (Disk) obj;
        if (this.isWhite != other.isWhite) {
            return false;
        }
        return true;
    }
}