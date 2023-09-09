package com.RuinedEngine.entity;

import org.joml.Vector3f;

public class Entity {
	private Model model;
	private Vector3f pos;
	private Vector3f rotation;
	private float scale;
	private boolean selected;
	public Entity(Model model, Vector3f pos, Vector3f rotation, float scale) {
		this.model = model;
		this.pos = pos;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public void incPos(float x, float y, float z) {
		this.pos.x += x;
		this.pos.y += y;
		this.pos.z += z;
	}
	public void setPos(float x, float y, float z) {
		this.pos.x = x;
		this.pos.y = y;
		this.pos.z = z;
	}
	public void incRotation(float x, float y, float z) {
		this.rotation.x += x;
		this.rotation.y += y;
		this.rotation.z += z;
	}
	public void setRotation(float x, float y, float z) {
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean getSelected() {
		return selected;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public Vector3f getPos() {
		return pos;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}
	
}
