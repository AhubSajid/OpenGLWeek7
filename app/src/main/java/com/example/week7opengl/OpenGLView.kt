package com.example.week7opengl
import android.opengl.GLSurfaceView
import android.content.Context
import android.opengl.GLES20
import android.util.AttributeSet
import android.util.Log
import freemap.openglwrapper.Camera
import freemap.openglwrapper.GLMatrix
import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.io.IOException
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10


class OpenGLView(ctx: Context, aset: AttributeSet)  :GLSurfaceView(ctx,aset), GLSurfaceView.Renderer {
    init {
        setEGLContextClientVersion(2) // specify OpenGL ES 2.0
        setRenderer(this) // set the renderer for this GLSurfaceView
    }
    val gpu = GPUInterface("DefaultShaderInterface")
    var fbuf : FloatBuffer? = null
    val blue = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)
    val yellow = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
    val viewMatrix = GLMatrix()
   val projMatrix = GLMatrix()

    val camera = Camera(0f,0f,0f)

    // We initialise the rendering here
    override fun onSurfaceCreated(unused: GL10, config: javax.microedition.khronos.egl.EGLConfig) {
        // Set the background colour (red=0, green=0, blue=0, alpha=1)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Enable depth testing - will cause nearer 3D objects to automatically
        // be drawn over further objects
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        try {
            val success = gpu.loadShaders(context.assets, "vertex.glsl", "fragment.glsl")
            if (!success) {
                Log.e("OpenGLBasic", gpu.lastShaderError)
            }
            fbuf = OpenGLUtils.makeFloatBuffer(
                floatArrayOf(
                0f, 0f, -3f,
                1f, 0f, -3f,
                0.5f, 1f, -3f,
                    -0.5f,0f,-6f,
                    0.5f,0f,-6f,
                    0f,1f,-6f
            ))

            gpu.select()
        } catch (e: IOException) {
            Log.e("OpenGLBasic", e.stackTraceToString())
        }
    }

    // We draw our shapes here
    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        fbuf?.apply {
            val ref_aVertex = gpu.getAttribLocation("aVertex")
            val ref_uColour = gpu.getUniformLocation("uColour")
            viewMatrix.setAsIdentityMatrix()
            val refUView = gpu.getUniformLocation("uView")
            gpu.sendMatrix(refUView,viewMatrix)

            val refUProj = gpu.getUniformLocation("uProjection")
            gpu.sendMatrix(refUProj,projMatrix)

            Log.d("OpenGLBasic", "Shader vars: $ref_aVertex $ref_uColour $refUView $refUProj")

            gpu.setUniform4FloatArray(ref_uColour, blue)
            gpu.specifyBufferedDataFormat(ref_aVertex, this, 0)
            gpu.drawBufferedTriangles(0,3)

            gpu.setUniform4FloatArray(ref_uColour, yellow)
            gpu.drawBufferedTriangles(3,3)
        }


    }

    // Used if the screen is resized
    override fun onSurfaceChanged(unused: GL10, w: Int, h: Int) {
        GLES20.glViewport(0,0,w,h)
        val hfov = 60.0f
        val aspect : Float = w.toFloat()/h
        projMatrix.setProjectionMatrix(hfov,aspect,0.001f,100f)
    }
}
