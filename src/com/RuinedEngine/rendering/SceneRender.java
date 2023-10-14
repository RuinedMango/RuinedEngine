package com.RuinedEngine.rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.RuinedEngine.VFX.Fog;
import com.RuinedEngine.core.Scene;
import com.RuinedEngine.entity.Entity;
import com.RuinedEngine.entity.Material;
import com.RuinedEngine.entity.Mesh;
import com.RuinedEngine.entity.Model;
import com.RuinedEngine.entity.Texture;
import com.RuinedEngine.lighting.AmbientLight;
import com.RuinedEngine.lighting.CascadeShadow;
import com.RuinedEngine.lighting.DirLight;
import com.RuinedEngine.lighting.PointLight;
import com.RuinedEngine.lighting.SceneLights;
import com.RuinedEngine.lighting.SpotLight;
import com.RuinedEngine.utils.AnimationData;
import com.RuinedEngine.utils.Consts;
import com.RuinedEngine.utils.TextureCache;
import com.RuinedEngine.utils.UniformsMap;

public class SceneRender {
	private ShaderProgram shaderProgram;
	private UniformsMap uniformsMap;
	
	public SceneRender() {
		List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
		shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.vert", GL20.GL_VERTEX_SHADER));
		shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.frag", GL20.GL_FRAGMENT_SHADER));
		shaderProgram = new ShaderProgram(shaderModuleDataList);
		createUniforms();
	}
	private void createUniforms() {
		uniformsMap = new UniformsMap(shaderProgram.getProgramId());
		uniformsMap.createUniform("bonesMatrices");
		uniformsMap.createUniform("projectionMatrix");
		uniformsMap.createUniform("viewMatrix");
		uniformsMap.createUniform("modelMatrix");
		uniformsMap.createUniform("txtSampler");
		uniformsMap.createUniform("normalSampler");
		uniformsMap.createUniform("material.ambient");
		uniformsMap.createUniform("material.diffuse");
		uniformsMap.createUniform("material.specular");
		uniformsMap.createUniform("material.reflectance");
		uniformsMap.createUniform("material.hasNormalMap");
		uniformsMap.createUniform("ambientLight.factor");
		uniformsMap.createUniform("ambientLight.color");
		for(int i = 0; i < Consts.MAX_POINT_LIGHTS; i++) {
			String name = "pointLights[" + i + "]";
			uniformsMap.createUniform(name + ".position");
			uniformsMap.createUniform(name + ".color");
			uniformsMap.createUniform(name + ".intensity");
			uniformsMap.createUniform(name + ".att.constant");
			uniformsMap.createUniform(name + ".att.linear");
			uniformsMap.createUniform(name + ".att.exponent");
		}
		for(int i = 0; i < Consts.MAX_SPOT_LIGHTS; i++) {
			String name = "spotLights[" + i + "]";
			uniformsMap.createUniform(name + ".pl.position");
			uniformsMap.createUniform(name + ".pl.color");
			uniformsMap.createUniform(name + ".pl.intensity");
			uniformsMap.createUniform(name + ".pl.att.constant");
			uniformsMap.createUniform(name + ".pl.att.linear");
			uniformsMap.createUniform(name + ".pl.att.exponent");
			uniformsMap.createUniform(name + ".conedir");
			uniformsMap.createUniform(name + ".cutoff");
		}
		uniformsMap.createUniform("dirLight.color");
		uniformsMap.createUniform("dirLight.direction");
		uniformsMap.createUniform("dirLight.intensity");
		uniformsMap.createUniform("fog.activeFog");
		uniformsMap.createUniform("fog.color");
		uniformsMap.createUniform("fog.density");
		for(int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
			uniformsMap.createUniform("shadowMap_" + i);
			uniformsMap.createUniform("cascadeshadows["+i+"]" + ".projViewMatrix");
			uniformsMap.createUniform("cascadeshadows["+i+"]" + ".splitDistance");
		}
	}
	private void updateLights(Scene scene) {
		Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
		SceneLights sceneLights = scene.getSceneLights();
		AmbientLight ambientLight = sceneLights.getAmbientLight();
		uniformsMap.setUniform("ambientLight.factor", ambientLight.getIntensity());
		uniformsMap.setUniform("ambientLight.color", ambientLight.getColor());
		
		DirLight dirLight = sceneLights.getDirLight();
		Vector4f auxDir = new Vector4f(dirLight.getDirection(), 0);
		auxDir.mul(viewMatrix);
		Vector3f dir = new Vector3f(auxDir.x, auxDir.y, auxDir.z);
		uniformsMap.setUniform("dirLight.color", dirLight.getColor());
		uniformsMap.setUniform("dirLight.direction", dir);
		uniformsMap.setUniform("dirLight.intensity", dirLight.getIntensity());
		List<PointLight> pointLights = sceneLights.getPointLights();
		int numPointLights = pointLights.size();
		PointLight pointLight;
		for(int i = 0; i < Consts.MAX_POINT_LIGHTS; i++) {
			if(i < numPointLights) {
				pointLight = pointLights.get(i);
			}else {
				pointLight = null;
			}
			String name = "pointLights[" + i + "]";
			updatePointLight(pointLight, name, viewMatrix);
		}
		List<SpotLight> spotLights = sceneLights.getSpotLights();
		int numSpotLights = spotLights.size();
		SpotLight spotLight;
		for(int i = 0; i < Consts.MAX_SPOT_LIGHTS; i++) {
			if(i < numSpotLights) {
				spotLight = spotLights.get(i);
			}else {
				spotLight = null;
			}
			String name = "spotLights[" + i + "]";
			updateSpotLight(spotLight, name, viewMatrix);
		}
	}
	private void updatePointLight(PointLight pointLight, String prefix, Matrix4f viewMatrix) {
		Vector4f aux = new Vector4f();
		Vector3f lightPosition = new Vector3f();
		Vector3f color = new Vector3f();
		float intensity = 0.0f;
		float constant = 0.0f;
		float linear = 0.0f;
		float exponent = 0.0f;
		if(pointLight != null) {
			aux.set(pointLight.getPosition(),1);
			aux.mul(viewMatrix);
			lightPosition.set(aux.x, aux.y, aux.z);
			color.set(pointLight.getColor());
			intensity = pointLight.getIntensity();
			PointLight.Attenuation attenuation = pointLight.getAttenuation();
			constant = attenuation.getConstant();
			linear = attenuation.getLinear();
			exponent = attenuation.getExponent();
		}
		uniformsMap.setUniform(prefix + ".position", lightPosition);
		uniformsMap.setUniform(prefix + ".color", color);
		uniformsMap.setUniform(prefix + ".intensity", intensity);
		uniformsMap.setUniform(prefix + ".att.constant", constant);
		uniformsMap.setUniform(prefix + ".att.linear", linear);
		uniformsMap.setUniform(prefix + ".att.exponent", exponent);
	}
	private void updateSpotLight(SpotLight spotLight, String prefix, Matrix4f viewMatrix) {
		PointLight pointLight = null;
		Vector3f coneDirection = new Vector3f();
		float cutoff = 0.0f;
		if(spotLight != null) {
			coneDirection = spotLight.getConeDirection();
			cutoff = spotLight.getCutOff();
			pointLight = spotLight.getPointLight();
		}
		uniformsMap.setUniform(prefix + ".conedir", coneDirection);
		uniformsMap.setUniform(prefix + ".conedir", cutoff);
		updatePointLight(pointLight, prefix + ".pl", viewMatrix);
	}
	public void cleanup() {
		shaderProgram.cleanup();
	}
	public void render(Scene scene, ShadowRender shadowRender) {
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		shaderProgram.bind();
		
		uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
		uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
		uniformsMap.setUniform("txtSampler", 0);
		uniformsMap.setUniform("normalSampler", 1);
		
		int start = 2;
		List<CascadeShadow> cascadeShadows = shadowRender.getCascadeShadow();
		for(int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
			uniformsMap.setUniform("shadowMap_"+i, start+i);
			CascadeShadow cascadeShadow = cascadeShadows.get(i);
			uniformsMap.setUniform("cascadeshadows["+i+"]" + ".projViewMatrix", cascadeShadow.getProjViewMatrix());
			uniformsMap.setUniform("cascadeshadows["+i+"]" + ".splitDistance", cascadeShadow.getSplitDistance());
		}
		
		shadowRender.getShadowBuffer().bindTextures(GL20.GL_TEXTURE2);
		
		updateLights(scene);
		
		Fog fog = scene.getFog();
		uniformsMap.setUniform("fog.activeFog", fog.isActive() ? 1 : 0);
		uniformsMap.setUniform("fog.color", fog.getColor());
		uniformsMap.setUniform("fog.density", fog.getDensity());
		

		Collection<Model> models = scene.getModelMap().values();
		TextureCache textureCache = scene.getTextureCache();
		for(Model model : models) {
			List<Entity> entities = model.getEntitiesList();
			
			for(Material material : model.getMaterialList()) {
				uniformsMap.setUniform("material.ambient", material.getAmbientColor());
				uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());
				uniformsMap.setUniform("material.specular", material.getSpecularColor());
				uniformsMap.setUniform("material.reflectance", material.getReflectance());
				String normalMapPath = material.getNormalMapPath();
				boolean hasNormalMapPath = normalMapPath != null;
				uniformsMap.setUniform("material.hasNormalMap", hasNormalMapPath ? 1 : 0);
				Texture texture = textureCache.getTexture(material.getTexturePath());
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				texture.bind();
				if(hasNormalMapPath) {
					Texture normalMapTexture = textureCache.getTexture(normalMapPath);
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					normalMapTexture.bind();
				}

				
				for(Mesh mesh : material.getMeshList()) {
					GL30.glBindVertexArray(mesh.getVaoId());
					for(Entity entity : entities) {
						uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
						AnimationData animationData = entity.getAnimationData();
						if(animationData == null) {
							uniformsMap.setUniform("bonesMatrices", AnimationData.DEFAULT_BONES_MATRICES);
						}else {
							uniformsMap.setUniform("bonesMatrices", animationData.getCurrentFrame().boneMatrices());
						}
						GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getNumVertices(), GL11.GL_UNSIGNED_INT, 0);
					}
				}
			}
		}
		
		GL30.glBindVertexArray(0);
		
		shaderProgram.unbind();
	}
}
