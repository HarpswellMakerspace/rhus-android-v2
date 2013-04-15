package org.calflora.observer.model;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

public class Plant {
	
	private String taxon;
	private String common;
	private int nstatus;
	private String lifeform;
	private String family;
	private String photoid;
	
	public Drawable getThumbnail(Context context) {
		AssetManager assets = context.getResources().getAssets();
		AssetFileDescriptor asset = null;
		Drawable plantThumbnail = null;
		try {
			String imagePath = "plant_images/" + getPhotoid().replace("'","")+".jpeg";
			asset = assets.openFd(imagePath);
			plantThumbnail = Drawable.createFromStream(asset.createInputStream(), "");
		} catch (IOException e) {
			// TODO Show default image for plant
		}
		return plantThumbnail;
	}
	
	public String getTaxon() {
		return taxon;
	}
	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}
	public String getCommon() {
		return common;
	}
	public void setCommon(String common) {
		this.common = common;
	}
	public int getNstatus() {
		return nstatus;
	}
	public void setNstatus(int nstatus) {
		this.nstatus = nstatus;
	}
	public String getLifeform() {
		return lifeform;
	}
	public void setLifeform(String lifeform) {
		this.lifeform = lifeform;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getPhotoid() {
		return photoid;
	}
	public void setPhotoid(String photoid) {
		this.photoid = photoid;
	}
	
}
