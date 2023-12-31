package com.RuinedEngine.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL30.glGenBuffers;

public class Mesh {
	private int numVertices;
	private int vaoId;
	private List<Integer> vboIdList;
	private Vector3f aabbMax;
	private Vector3f aabbMin;
	public static final int MAX_WEIGHTS = 4;
	
	public Mesh(float[] positions, float[] normals, float[] tangents, float[] bitangents,float[] textCoords, int[] indices) {
		this(positions, normals, tangents, bitangents, textCoords, indices, 
				new int[Mesh.MAX_WEIGHTS * positions.length / 3], new float[Mesh.MAX_WEIGHTS * positions.length / 3], new Vector3f(), new Vector3f());
	}
	
	public Mesh(float[] positions, float[] normals, float[] tangents, float[] bitangents,float[] textCoords, int[] indices, int[] boneIndices, float[] weights, Vector3f aabbMin, Vector3f aabbMax) {
		try(MemoryStack stack = MemoryStack.stackPush()){
			this.aabbMin = aabbMin;
			this.aabbMax = aabbMax;
			numVertices = indices.length;
			vboIdList = new ArrayList<>();
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			//Positions VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
			positionsBuffer.put(0, positions);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			//Normals VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer normalsBuffer = stack.callocFloat(normals.length);
			normalsBuffer.put(0, normals);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(1);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
			
			//Tangents VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer tangentsBuffer = stack.callocFloat(tangents.length);
			tangentsBuffer.put(0, tangents);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tangentsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(2);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
			
			//Bitangents VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer bitangentsBuffer = stack.callocFloat(bitangents.length);
			bitangentsBuffer.put(0, bitangents);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bitangentsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(3);
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 0, 0);
			
			//Texture Coords VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer textCoordsBuffer = stack.callocFloat(textCoords.length);
			textCoordsBuffer.put(0, textCoords);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textCoordsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(4);
			GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 0, 0);
			
			//Bone weights
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer weightsBuffer = stack.callocFloat(weights.length);
			weightsBuffer.put(weights).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, weightsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(5);
			GL20.glVertexAttribPointer(5, 4, GL11.GL_FLOAT, false, 0, 0);
			
			//Bone indices
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			IntBuffer boneIndicesBuffer = stack.callocInt(boneIndices.length);
			boneIndicesBuffer.put(boneIndices).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, boneIndicesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(6);
			GL20.glVertexAttribPointer(6, 4, GL11.GL_FLOAT, false, 0, 0);
			
			//Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			IntBuffer indicesBuffer = stack.callocInt(indices.length);
			indicesBuffer.put(0, indices);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		}
	}
	public void cleanup() {
		vboIdList.forEach(GL30::glDeleteBuffers);
		GL30.glDeleteVertexArrays(vaoId);
	}
	public int getNumVertices() {
		return numVertices;
	}
	public final int getVaoId() {
		return vaoId;
	}
	public Vector3f getAabbMax() {
		return aabbMax;
	}
	public Vector3f getAabbMin() {
		return aabbMin;
	}
}
