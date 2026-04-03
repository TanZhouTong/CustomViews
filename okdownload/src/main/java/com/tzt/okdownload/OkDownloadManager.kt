package com.tzt.okdownload

import android.util.Log
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.SpeedCalculator
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.StatusUtil.Status
import com.liulishuo.okdownload.UnifiedListenerManager
import com.liulishuo.okdownload.core.breakpoint.BlockInfo
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend
import java.util.concurrent.ConcurrentHashMap

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/14 15:24
 */
object OkDownloadManager {
    private val contexts = ConcurrentHashMap<String, DownloadContext>()

    fun downloadTaskApiTest() {
        val builder: DownloadTask.Builder = DownloadTask.Builder(url, file)

        // Set the minimum internal milliseconds of progress callbacks to 100ms.(default is 3000)
        builder.setMinIntervalMillisCallbackProcess(100)

        // set the priority of the task to 10, higher means less time to wait to download.(default is 0)
        builder.setPriority(10)

        // set the read buffer to 8192 bytes for the response input-stream.(default is 4096)
        builder.setReadBufferSize(8192)

        // set the flush buffer to 32768 bytes for the buffered output-stream.(default is 16384)
        builder.setFlushBufferSize(32768)

        // set this task allow using 5 connections to download data.
        builder.setConnectionCount(5)

        // build the task.
        val task = builder.build()
        task.tag
        task.addTag()
    }

    fun addTaskToNetDisk(netDiskId: String, task: DownloadTask, listener: DownloadListener) {
        var context = contexts[netDiskId]

        if (context == null) {
            val set = DownloadContext.QueueSet()
            val contextBuilder = set.setParentPathFile(task.parentFile)// 设置公共保存路径
                .commit()// 提交 QueueSet 配置
            contextBuilder.bindSetTask(task)
            context = contextBuilder.build()
            contexts[netDiskId] = context
            // 串行
            context.start(listener, true)
        } else {
            context.start(listener, true)
            // 如果 context 已存在，动态向 context 关联的任务序列添加任务
            // 注意：OkDownload 的 Context 在 start 后动态添加任务较复杂，
            // 这种情况通常建议直接使用方案一的 SerialQueue。
        }
    }

    private fun apiTest(context: DownloadContext) {
        // global
        // 清除cache item数据
        OkDownload.with().breakpointStore().remove()
        OkDownload.with().downloadDispatcher().cancel()
        EndCause.COMPLETED  // 任务完成
        EndCause.CANCELED   // 任务暂停
        EndCause.ERROR  // 任务失败
        //
        OkDownload.with().downloadDispatcher().cancel()
        //
    }

    private fun unifiedListenerManagerTest(task: DownloadTask) {
        val unifiedListenerManager = UnifiedListenerManager()
        unifiedListenerManager.attachListener(task, listener)
        unifiedListenerManager.detachListener(task, listener)
        unifiedListenerManager.addAutoRemoveListenersWhenTaskEnd(task.id)
        unifiedListenerManager.enqueueTaskWithUnifiedListener()
        unifiedListenerManager.executeTaskWithUnifiedListener()
    }

    private fun combineApiTest(task: DownloadTask) {
        val unifiedListenerManager = UnifiedListenerManager()
        unifiedListenerManager.attachListener(task, listener)
        // end时自动detach，就不需要考虑异步导致的问题
        unifiedListenerManager.addAutoRemoveListenersWhenTaskEnd(task.id)

        addTaskToNetDisk("onedrive", task, unifiedListenerManager.hostListener)
    }


    private fun statusApiTest(task: DownloadTask) {
        val status = StatusUtil.getStatus(task)
        Status.PENDING
        Status.RUNNING
        Status.IDLE
        Status.COMPLETED
        Status.UNKNOWN
    }

    /**
     * 区分具体网盘
     * */
    object listener : DownloadListener {
        // 保存一个网盘对应的repository
        override fun taskStart(task: DownloadTask) {
            // todo okdownloader中下载没有采用temp文件，判断文件下载是否真的成功需要okdownloader下载数据库与StatusUtils的状态来判断 -> 本地的下载数据库
            //
            task.file

            Log.d("listener", "taskStart: $task")
        }

        override fun connectTrialStart(
            task: DownloadTask,
            requestHeaderFields: Map<String?, List<String?>?>,
        ) {
            Log.d("listener", "connectTrialStart: $task")
        }

        override fun connectTrialEnd(
            task: DownloadTask,
            responseCode: Int,
            responseHeaderFields: Map<String?, List<String?>?>,
        ) {
            Log.d("listener", "connectTrialEnd: $task")
        }

        override fun downloadFromBeginning(
            task: DownloadTask,
            info: BreakpointInfo,
            cause: ResumeFailedCause,
        ) {
            TODO("Not yet implemented")
        }

        override fun downloadFromBreakpoint(
            task: DownloadTask,
            info: BreakpointInfo,
        ) {
            TODO("Not yet implemented")
        }

        override fun connectStart(
            task: DownloadTask,
            blockIndex: Int,
            requestHeaderFields: Map<String?, List<String?>?>,
        ) {
            TODO("Not yet implemented")
        }

        override fun connectEnd(
            task: DownloadTask,
            blockIndex: Int,
            responseCode: Int,
            responseHeaderFields: Map<String?, List<String?>?>,
        ) {
            TODO("Not yet implemented")
        }

        override fun fetchStart(
            task: DownloadTask,
            blockIndex: Int,
            contentLength: Long,
        ) {
            TODO("Not yet implemented")
        }

        override fun fetchProgress(
            task: DownloadTask,
            blockIndex: Int,
            increaseBytes: Long,
        ) {
            TODO("Not yet implemented")
        }

        override fun fetchEnd(
            task: DownloadTask,
            blockIndex: Int,
            contentLength: Long,
        ) {
            TODO("Not yet implemented")
        }

        override fun taskEnd(
            task: DownloadTask,
            cause: EndCause,
            realCause: Exception?,
        ) {
            TODO("Not yet implemented")
        }
    }

    object listenerSpeed : DownloadListener4WithSpeed() {
        override fun infoReady(
            task: DownloadTask,
            info: BreakpointInfo,
            fromBreakpoint: Boolean,
            model: Listener4SpeedAssistExtend.Listener4SpeedModel,
        ) {
            TODO("Not yet implemented")
        }

        override fun progressBlock(
            task: DownloadTask,
            blockIndex: Int,
            currentBlockOffset: Long,
            blockSpeed: SpeedCalculator,
        ) {
            TODO("Not yet implemented")
        }

        override fun progress(
            task: DownloadTask,
            currentOffset: Long,
            taskSpeed: SpeedCalculator,
        ) {
            TODO("Not yet implemented")
        }

        override fun blockEnd(
            task: DownloadTask,
            blockIndex: Int,
            info: BlockInfo?,
            blockSpeed: SpeedCalculator,
        ) {
            TODO("Not yet implemented")
        }

        override fun taskEnd(
            task: DownloadTask,
            cause: EndCause,
            realCause: java.lang.Exception?,
            taskSpeed: SpeedCalculator,
        ) {
            TODO("Not yet implemented")
        }

        override fun taskStart(task: DownloadTask) {
            TODO("Not yet implemented")
        }

        override fun connectStart(
            task: DownloadTask,
            blockIndex: Int,
            requestHeaderFields: Map<String?, List<String?>?>,
        ) {
            TODO("Not yet implemented")
        }

        override fun connectEnd(
            task: DownloadTask,
            blockIndex: Int,
            responseCode: Int,
            responseHeaderFields: Map<String?, List<String?>?>,
        ) {
            TODO("Not yet implemented")
        }
    }

}