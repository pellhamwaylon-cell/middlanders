package com.middlanders.core;
import org.teavm.jso.webgl.*;
import org.teavm.jso.typedarrays.*;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.JSBody;

public class WebRenderer {
    private WebGLRenderingContext gl;
    private WebGLProgram prog;
    private WebGLBuffer vbo, tbo, ibo;
    private WebGLUniformLocation yL, pL, aL, sL, oL, pXL;
    private WebGLTexture tex;
    public WebRenderer(HTMLCanvasElement c) { gl = (WebGLRenderingContext)c.getContext("webgl2"); }
    @JSBody(params={"gl","t"}, script="var i=window.engineData.activeTexture; gl.bindTexture(gl.TEXTURE_2D,t); if(i){gl.texImage2D(gl.TEXTURE_2D,0,gl.RGBA,gl.RGBA,gl.UNSIGNED_BYTE,i); gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST); gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);}")
    public static native void bindTex(WebGLRenderingContext gl, WebGLTexture t);
    public void init() {
        String vs = "attribute vec3 p; attribute vec2 t; varying vec2 vT; uniform float y, pi, a; uniform vec3 off, pos; void main(){ vec3 wp = p + off - pos; float cp=cos(-pi), sp=sin(-pi), cy=cos(-y), sy=sin(-y); mat4 rx=mat4(1,0,0,0, 0,cp,-sp,0, 0,sp,cp,0, 0,0,0,1); mat4 ry=mat4(cy,0,sy,0, 0,1,0,0, -sy,0,cy,0, 0,0,0,1); float f=1.0/tan(0.78); mat4 pr=mat4(f/a,0,0,0, 0,f,0,0, 0,0,-1,-1, 0,0,-0.2,0); gl_Position = pr * rx * ry * vec4(wp,1); vT=t; }";
        String fs = "precision mediump float; varying vec2 vT; uniform sampler2D s; void main(){ gl_FragColor=texture2D(s,vT); }";
        WebGLShader vS = gl.createShader(gl.VERTEX_SHADER); gl.shaderSource(vS,vs); gl.compileShader(vS);
        WebGLShader fS = gl.createShader(gl.FRAGMENT_SHADER); gl.shaderSource(fS,fs); gl.compileShader(fS);
        prog = gl.createProgram(); gl.attachShader(prog,vS); gl.attachShader(prog,fS); gl.linkProgram(prog);
        float[] v = {-0.5f,-0.5f,0.5f, 0.5f,-0.5f,0.5f, 0.5f,0.5f,0.5f, -0.5f,0.5f,0.5f, -0.5f,-0.5f,-0.5f, 0.5f,-0.5f,-0.5f, 0.5f,0.5f,-0.5f, -0.5f,0.5f,-0.5f};
        Float32Array vA = Float32Array.create(v.length); for(int i=0;i<v.length;i++) vA.set(i,v[i]);
        vbo = gl.createBuffer(); gl.bindBuffer(gl.ARRAY_BUFFER,vbo); gl.bufferData(gl.ARRAY_BUFFER,vA,gl.STATIC_DRAW);
        float[] tc = {0,0, 1,0, 1,1, 0,1, 0,0, 1,0, 1,1, 0,1};
        Float32Array tA = Float32Array.create(tc.length); for(int i=0;i<tc.length;i++) tA.set(i,tc[i]);
        tbo = gl.createBuffer(); gl.bindBuffer(gl.ARRAY_BUFFER,tbo); gl.bufferData(gl.ARRAY_BUFFER,tA,gl.STATIC_DRAW);
        short[] ids = {0,1,2, 0,2,3, 5,4,7, 5,7,6, 3,2,6, 3,6,7, 4,5,1, 4,1,0, 1,5,6, 1,6,2, 4,0,3, 4,3,7};
        Int16Array iA = Int16Array.create(ids.length); for(int i=0;i<ids.length;i++) iA.set(i,ids[i]);
        ibo = gl.createBuffer(); gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,ibo); gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,iA,gl.STATIC_DRAW);
        yL=gl.getUniformLocation(prog,"y"); pL=gl.getUniformLocation(prog,"pi"); aL=gl.getUniformLocation(prog,"a");
        sL=gl.getUniformLocation(prog,"s"); oL=gl.getUniformLocation(prog,"off"); pXL=gl.getUniformLocation(prog,"pos");
        tex = gl.createTexture(); bindTex(gl, tex);
        gl.clearColor(0.5f, 0.8f, 1f, 1f); gl.enable(gl.DEPTH_TEST);
    }
    public void renderFrame(double yaw, double pitch, double aspect, double x, double y, double z) {
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT); gl.useProgram(prog);
        gl.uniform1f(yL,(float)yaw); gl.uniform1f(pL,(float)pitch); gl.uniform1f(aL,(float)aspect);
        gl.uniform3f(pXL,(float)x,(float)y,(float)z);
        int pA = gl.getAttribLocation(prog,"p"); gl.enableVertexAttribArray(pA);
        gl.bindBuffer(gl.ARRAY_BUFFER,vbo); gl.vertexAttribPointer(pA,3,gl.FLOAT,false,0,0);
        int tA = gl.getAttribLocation(prog,"t"); gl.enableVertexAttribArray(tA);
        gl.bindBuffer(gl.ARRAY_BUFFER,tbo); gl.vertexAttribPointer(tA,2,gl.FLOAT,false,0,0);
        gl.activeTexture(gl.TEXTURE0); gl.bindTexture(gl.TEXTURE_2D,tex); gl.uniform1i(sL,0);
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,ibo);
        for(int ix=-8; ix<8; ix++) {
            for(int iz=-8; iz<8; iz++) {
                gl.uniform3f(oL, (float)ix, 0.0f, (float)iz);
                gl.drawElements(gl.TRIANGLES, 36, gl.UNSIGNED_SHORT, 0);
            }
        }
    }
}
