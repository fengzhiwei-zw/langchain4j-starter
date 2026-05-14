package com.feng.langchain4jstarter;

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationMessage;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationParam;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;

import java.util.Collections;

public class Main {

    static {
        // 以下为北京地域url，各地域的base_url不同
        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
    }

    // 若没有配置环境变量，请用百炼API Key将下行替换为：apiKey="sk-xxx"
    // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
    static String apiKey = System.getenv("DASHSCOPE_API_KEY");

    public static ImageGenerationResult waitTask(String taskId)
            throws ApiException, NoApiKeyException {
        ImageGeneration imageGeneration = new ImageGeneration();
        return imageGeneration.wait(taskId, apiKey);
    }

    public static void asyncCall() throws ApiException, NoApiKeyException, UploadFileException {
        ImageGenerationMessage message = ImageGenerationMessage.builder()
                .role("user")
                .content(Collections.singletonList(
                        Collections.singletonMap("text", "一位年轻女性，自然随性的自拍风格，超高清写实人物生活照。她身着黄色碎花长袖上衣，长发自然垂落且略带波浪卷。画面背景为户外自然景色，近处有绿植，远处可见水域和山峦。自然柔和的阳光洒在人物脸上和身上，形成自然的光影效果，拍摄机位为人物手持设备的中景自拍视角，人物身体自然站立，展现出轻松自在的状态。角度自然，随手一拍的快照风格，不经意间的抓拍。")
                )).build();

        ImageGenerationParam param = ImageGenerationParam.builder()
                .apiKey(apiKey)
                .model("wan2.7-image-pro")
                .messages(Collections.singletonList(message))
                .enableSequential(false)
                .n(1)
                .size("2K")
                .build();

        ImageGeneration imageGeneration = new ImageGeneration();
        ImageGenerationResult taskResult = null;
        try {
            System.out.println("----async call, creating task----");
            taskResult = imageGeneration.asyncCall(param);
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            throw new RuntimeException(e.getMessage());
        }
        System.out.println("Task created: " + JsonUtils.toJson(taskResult));

        // 等待任务完成
        String taskId = taskResult.getOutput().getTaskId();
        ImageGenerationResult result = waitTask(taskId);
        System.out.println(JsonUtils.toJson(result));
    }

    public static void main(String[] args) {
        try {
            asyncCall();
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
        }
    }
}