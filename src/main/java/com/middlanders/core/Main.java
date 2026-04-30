package com.middlanders.core;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.browser.Window;
import org.teavm.jso.JSBody;

public class Main {
    private static WebRenderer renderer;
    private static double px = 0, py = 2, pz = 0, yaw = 0, pitch = 0;
    @JSBody(script = "return window.engineData.yaw;") public static native double getYaw();
    @JSBody(script = "return window.engineData.pitch;") public static native double getPitch();
    @JSBody(params={"k"}, script = "return window.engineData.keys[k] === true;") public static native boolean isDown(String k);
    @JSBody(script = "return window.engineData.state;") public static native String getState();
    public static void main(String[] args) {
        HTMLCanvasElement canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        canvas.setWidth(Window.current().getInnerWidth());
        canvas.setHeight(Window.current().getInnerHeight());
        HTMLDocument.current().getElementById("game_frame").appendChild(canvas);
        renderer = new WebRenderer(canvas);
        renderer.init();
        runLoop();
    }
    private static void runLoop() {
        Window.requestAnimationFrame(ts -> {
            if ("PLAYING".equals(getState())) {
                yaw = getYaw(); pitch = getPitch();
                double s = 0.08;
                if(isDown("KeyW")) { px += Math.sin(yaw)*s; pz -= Math.cos(yaw)*s; }
                if(isDown("KeyS")) { px -= Math.sin(yaw)*s; pz += Math.cos(yaw)*s; }
                if(isDown("KeyA")) { px -= Math.cos(yaw)*s; pz -= Math.sin(yaw)*s; }
                if(isDown("KeyD")) { px += Math.cos(yaw)*s; pz += Math.sin(yaw)*s; }
            } else { yaw = ts/2000.0; }
            HTMLDocument.current().getElementById("xyz").setInnerHTML(String.format("%.1f / %.1f / %.1f", px, py, pz));
            double aspect = (double)Window.current().getInnerWidth()/Window.current().getInnerHeight();
            renderer.renderFrame(yaw, pitch, aspect, px, py, pz);
            runLoop();
        });
    }
}
