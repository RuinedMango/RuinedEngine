package com.RuinedEngine.utils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

public class UniformsMap {
	private int programId;
	private Map<String, Integer> uniforms;
	
	public UniformsMap(int programId) {
		this.programId = programId;
		uniforms = new HashMap<>();
	}
	public void createUniform(String uniformName) {
		int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
		if(uniformLocation < 0) {
			throw new RuntimeException("Could not find uniform [" + uniformName + "] in shader program [" + programId + "]");
		}
		uniforms.put(uniformName, uniformLocation);
	}
	private int getUniformLocation(String uniformName) {
		Integer location = uniforms.get(uniformName);
		if(location == null) {
			throw new RuntimeException("Could not find uniform [" + uniformName + "]");
		}
		return location.intValue();
	}
	public void setUniform(String uniformName, int value) {
		GL20.glUniform1i(getUniformLocation(uniformName), value);
	}
	public void setUniform(String uniformName, float value) {
		GL20.glUniform1f(getUniformLocation(uniformName), value);
	}
	public void setUniform(String uniformName, Vector3f value) {
		GL20.glUniform3f(getUniformLocation(uniformName), value.x, value.y, value.z);
	}
	public void setUniform(String uniformName, Matrix4f value) {
		try(MemoryStack stack = MemoryStack.stackPush()){
			GL20.glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}
	public void setUniform(String uniformName, Matrix4f[] matrices) {
		try(MemoryStack stack = MemoryStack.stackPush()){
			int length = matrices != null ? matrices.length : 0;
			FloatBuffer fb = stack.mallocFloat(16 * length);
			for(int i = 0; i < length; i++) {
				matrices[i].get(16 * i, fb);
			}
			GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}
	public void setUniform(String uniformName, Vector4f value) {
		GL20.glUniform4f(getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
	}
	public void setUniform(String uniformName, Vector2f value) {
		GL20.glUniform2f(getUniformLocation(uniformName), value.x, value.y);
	}
}
