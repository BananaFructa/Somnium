package BananaFructa.somnium.service;

import BananaFructa.somnium.Config;
import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.embed.OllamaEmbedRequest;
import io.github.ollama4j.models.embed.OllamaEmbedResult;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.utils.OptionsBuilder;
import net.minecraft.nbt.CompoundTag;

import java.io.File;
import java.util.*;

public class OllamaServiceHandler {

    private boolean isDownErrorDebounce = false;

    public String address;
    public final Ollama ollama;

    public HashMap<String, List<OllamaChatMessage>> sessionHistory = new HashMap<>();

    public CompoundTag writeSession(List<OllamaChatMessage> history) throws JsonProcessingException {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size",history.size());
        for (int i = 0;i < history.size();i++) {
            ObjectMapper mapper = new ObjectMapper();
            tag.putString("message_" + i, mapper.writeValueAsString(history.get(i)));
        }
        return tag;
    }

    public List<OllamaChatMessage> readSession(CompoundTag tag) throws JsonProcessingException {
        int size = tag.getInt("size");
        List<OllamaChatMessage> history = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            ObjectMapper mapper = new ObjectMapper();
            history.add(mapper.readValue(tag.getString("message_" + i),OllamaChatMessage.class));
        }
        return history;
    }

    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("session_count",sessionHistory.size());
        List<String> keySet = sessionHistory.keySet().stream().toList();
        for (int i = 0 ;i < keySet.size();i++) {
            try {
                tag.putString("session_name_"+i,keySet.get(i));
                tag.put("session_"+i,writeSession(sessionHistory.get(keySet.get(i))));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return tag;
    }

    public void readNBT(CompoundTag tag) {
        sessionHistory.clear();
        int size = tag.getInt("session_count");
        for (int i = 0;i < size;i++) {
            String key = tag.getString("session_name_"+i);
            try {
                List<OllamaChatMessage> history = readSession(tag.getCompound("session_"+i));
                sessionHistory.put(key,history);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadModel(String modelName) {
        try {
            ollama.pullModel(modelName);
        } catch (OllamaException err) {
            throw new RuntimeException(err);
        }
    }

    public boolean unloadModel(String modelName) {
        try {
            ollama.unloadModel(modelName);
            return true;
        } catch (OllamaException err) {
            throw new RuntimeException(err);
        }
    }

    public OllamaServiceHandler(String address) {
        this.address = address;
        ollama = new Ollama(address);
        ollama.setRequestTimeoutSeconds(60*5);
    }

    public List<List<Double>> embed(String model, List<String> data) {
        OllamaEmbedRequest request = new OllamaEmbedRequest(model,data);
        try {
            OllamaEmbedResult result = ollama.embed(request);
            return result.getEmbeddings();
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public List<Double> embed(String model, String data) {
        List<List<Double>> embedding = embed(model,Arrays.asList(data));
        if (embedding == null) return null;
        return embedding.get(0);
    }

    public String prompt(String modelName, String sessionName, String message, byte[] image, int contextWindow, boolean thinking) {
        loadModel(modelName);

        OptionsBuilder builder = new OptionsBuilder().setNumCtx(contextWindow).setTemperature(0.6f).setTopK(20).setTopP(0.95f).setRepeatPenalty(1);
        OllamaChatRequest request = OllamaChatRequest.builder();

        if (sessionName != null && sessionHistory.containsKey(sessionName)) {
            request = request.withMessages(sessionHistory.get(sessionName));
        }

        request = request.withModel(modelName).withMessage(OllamaChatMessageRole.USER,message).withTools(new ArrayList<>()).withOptions(builder.build()).withThinking(thinking).build();
        if (image != null) {
            // This is a bit of a hack but whatever
            List<OllamaChatMessage> messages = Utils.readDeclaredField(OllamaChatRequest.class, request, "messages");
            OllamaChatMessage last = messages.get(messages.size() - 1);
            last.setImages(new ArrayList<>() {{
                add(image);
            }});
        }

        OllamaChatResult result;

        try {
            result = ollama.chat(request, null);
        } catch (OllamaException e) {
            throw new RuntimeException(e);
        }

        if (Config.onlyOneLoaded) unloadModel(modelName);
        if (sessionName != null) sessionHistory.put(sessionName,result.getChatHistory());
        return result.getResponseModel().getMessage().getResponse();
    }

    public List<String> getAvailableModels() throws OllamaException {
        return ollama.listModels().stream().map(m->m.getModel()).toList();
    }

    public boolean ping() throws OllamaException {
        return ollama.ping();
    }

    public boolean hasSession(String name) {
        return sessionHistory.containsKey(name);
    }

    public boolean isDown() {
        boolean up = false;
        try {
            up = ping();
            isDownErrorDebounce = false;
        } catch (OllamaException e) {
            if (!isDownErrorDebounce) e.printStackTrace();
            isDownErrorDebounce = true;
        }
        return !up;
    }
}
