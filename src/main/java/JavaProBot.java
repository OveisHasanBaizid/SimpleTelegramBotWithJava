import org.apache.commons.io.FilenameUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class JavaProBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "javapro1_bot";
    }

    @Override
    public String getBotToken() {
        return "5654814942:AAEX0mgtFdw-vmtnrwAsmsnISvJT99w0zrs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        try {
            if (update.getMessage().hasDocument()){
                getDocument(update);
            }else if (update.getMessage().hasAudio()){
                getAudio(update);
            } else if (update.getMessage().hasPhoto()) {
                getPhoto(update);
            } else if (message.equals("/list")) {
                String list = getList();
                sendMessage(update.getMessage().getChatId(),list);
            } else if (checkNumber(message)) {
                findFiles(Integer.parseInt(message) , update.getMessage().getChatId());
            }else{
                sendMessage(update.getMessage().getChatId(),"command invalid");
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void getDocument(Update update) throws TelegramApiException {
        Document document = update.getMessage().getDocument();
        String file_id = document.getFileId();

        GetFile getFile = new GetFile(file_id);

        String path = execute(getFile).getFilePath();
        File file = downloadFile(path);
        file.renameTo(new File("Data/"+document.getFileName()));
    }
    public void getAudio(Update update) throws TelegramApiException {
        Audio audio = update.getMessage().getAudio();
        String file_id = audio.getFileId();

        GetFile getFile = new GetFile(file_id);

        String path = execute(getFile).getFilePath();
        File file = downloadFile(path);
        file.renameTo(new File("Data/"+audio.getFileName()));
    }
    public void getPhoto(Update update) throws TelegramApiException {
        List<PhotoSize> photos = update.getMessage().getPhoto();

        String file_id = photos.stream()
                .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                .findFirst()
                .orElse(null).getFileId();

        GetFile getFile = new GetFile(file_id);

        String path = execute(getFile).getFilePath();
        File file = downloadFile(path);
        file.renameTo(new File("Data/"+file.getName()));
    }
    public String getList(){
        StringBuilder stringBuilder = new StringBuilder();
        String[] listNameFiles = new File("Data").list();
        if (listNameFiles.length==0)
            stringBuilder.append("list is empty.");
        for (int i = 0; i < listNameFiles.length ; i++) {
            stringBuilder.append((i+1)+" - "+listNameFiles[i]+"\n");
        }
        return stringBuilder.toString();
    }
    public void sendMessage(long chatId , String text) throws TelegramApiException {
        SendMessage sMessage = new SendMessage();
        sMessage.setText(text);
        sMessage.setChatId(chatId);
        execute(sMessage);
    }
    public boolean checkNumber(String text){
        for (int i = 0; i <text.length() ; i++) {
            if (!Character.isDigit(text.charAt(i)))
                return false;
        }
        return true;
    }
    public void findFiles(int n , long chatId) throws TelegramApiException {
        File[] files = new File("Data").listFiles();
        if (n<1 || n>files.length){
            sendMessage(chatId , "number input invalid.");
        }
        String format = FilenameUtils.getExtension(files[n-1].getName());
        switch (format){
            case "tmp" -> sendPhoto(chatId , files[n-1]);
            case "mp3" -> sendAudio(chatId , files[n-1]);
            default -> sendDocument(chatId , files[n-1]);
        }
    }
    public void sendPhoto(long chatId , File photo) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(photo));
        execute(sendPhoto);
    }
    public void sendAudio(long chatId , File audio) throws TelegramApiException {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(new InputFile(audio));
        execute(sendAudio);
    }
    public void sendDocument(long chatId , File document) throws TelegramApiException {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(document));
        execute(sendDocument);
    }
}
