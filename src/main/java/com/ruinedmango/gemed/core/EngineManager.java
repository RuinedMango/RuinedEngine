package main.java.com.ruinedmango.gemed.core;

import org.lwjgl.glfw.GLFWErrorCallback;

import main.java.com.ruinedmango.gemed.core.utils.Consts;
import main.java.com.ruinedmango.gemed.test.Launcher;

import org.lwjgl.glfw.GLFW;



public class EngineManager {
	public static final long NANOSECOND = 1000000000L;
	public static final float FRAMERATE = 1000;
	
	private static int fps;
	private static float frametime = 1.0f / FRAMERATE;
	
	private boolean isRunning;
	
	private WindowManager window;
	private MouseInput mouseInput;
	private GLFWErrorCallback errorCallback;
	private ILogic gameLogic;
	
	private void init() throws Exception{
		GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		window = Launcher.getWindow();
		gameLogic = Launcher.getGame();
		mouseInput = new MouseInput();
		window.init();
		gameLogic.init();
		mouseInput.init();
	}
	
	public void start() throws Exception{
		init();
		if(isRunning) {
			return;
		}
		run();
	}

	public void run() {
		this.isRunning = true;
		int frames = 0;
		long frameCounter = 0;
		long lastTime = System.nanoTime();
		double unprocessedTime = 0;
		
		while(isRunning) {
			boolean render = false;
			long startTime = System.nanoTime();
			long passedTime = startTime - lastTime;
			lastTime = startTime;
			
			unprocessedTime += passedTime / (double)NANOSECOND;
			frameCounter += passedTime;
			input();
			while(unprocessedTime > frametime) {
				render = true;
				unprocessedTime -= frametime;
				
				if(window.windowShouldClose()) {
					stop();
				}
				if(frameCounter >= NANOSECOND) {
					setFps(frames);
					window.setTitle(Consts.TITLE + " FPS: " + getFps());
					frames = 0;
					frameCounter = 0;
				}
			}
			if(render) {
				update();
				render();
				frames++;
			}
			
		}
		cleanup();
	}
	
	private void stop() {
		if(!isRunning) {
			return;
		}
		isRunning = false;
	}
	
	private void input() {
		mouseInput.input();
		gameLogic.input();
	}
	private void render() {
		gameLogic.render();
		window.update();
	}
	private void update() {
		gameLogic.update(mouseInput);
	}
	private void cleanup() {
		window.cleanup();
		gameLogic.cleanup();
		errorCallback.free();
		GLFW.glfwTerminate();
	}

	public static int getFps() {
		return fps;
	}

	public static void setFps(int fps) {
		EngineManager.fps = fps;
	}
	
}
