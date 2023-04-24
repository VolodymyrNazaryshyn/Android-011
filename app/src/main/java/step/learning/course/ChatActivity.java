package step.learning.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story";
    private final String CHANNEL_ID = "chat_notification_channel"; // id канала уведомлений
    private final int POST_NOTIFICATION_REQUEST_CODE = 234; // код (случайный) для запроса разрешения на отправку уведомлений
    private EditText etAuthor ;
    private EditText etMessage ;
    private LinearLayout chatContainer;
    private ScrollView svContainer;
    private final List<ChatMessage> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private MediaPlayer incomingMessagePlayer;
    private Handler handler; // планировщик задач в одном потоке

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );

        handler = new Handler();

        etAuthor = findViewById( R.id.chat_et_author ) ;
        etMessage = findViewById( R.id.chat_et_message ) ;
        chatContainer = findViewById(R.id.chat_container);
        svContainer = findViewById(R.id.sv_container);
        incomingMessagePlayer = MediaPlayer.create(this, R.raw.sound_1);
        findViewById( R.id.chat_button_send ).setOnClickListener( this::sendMessageClick ) ;

        handler.post(this::updateChat);
    }

    private void updateChat() {
        new Thread(this::getChatMessages).start();
        handler.postDelayed(this::updateChat, 3000); // отложенный запуск
    }

    private void getChatMessages() {
        try (InputStream chatStream = new URL(CHAT_URL).openStream()) {
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = chatStream.read(chunk)) != -1) {
                byteBuilder.write(chunk, 0, len);
            }
            parseChatMessages(byteBuilder.toString());
            byteBuilder.close();
        }
        catch (android.os.NetworkOnMainThreadException ignored) {
            Log.d("getChatMessages", "NetworkOnMainThreadException");
        }
        catch (MalformedURLException ex) {
            Log.d("getChatMessages", "MalformedURLException " + ex.getMessage());
        }
        catch (IOException ex) {
            Log.d("getChatMessages", "IOException " + ex.getMessage());
        }
    }
    private void parseChatMessages(String loadedContent) {
        /* loadedContent =
            {
                "status": "success"
                "data": [ {}, {}, ... ]
            }
         */
        try {
            JSONObject content = new JSONObject(loadedContent);
            // TODO: check 'status' field for 'success' value
            if (content.has("data")) {
                JSONArray data = content.getJSONArray("data");
                boolean wasNewMessage = false;
                int len = data.length();
                synchronized (chatMessages) {
                    for (int i = 0; i < len; i++) {
                        // преобразуем сообщение из JSON
                        ChatMessage tmp = new ChatMessage(data.getJSONObject(i));
                        // проверяем есть ли такое сообщение в хранимом массиве
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (this.chatMessages.stream().noneMatch(
                                    msg -> msg.getId().equals(tmp.getId()))) { // по совпадению Id
                                this.chatMessages.add(tmp);
                                wasNewMessage = true;
                            }
                        }
                        if (wasNewMessage) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                this.chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
                                //this.chatMessages.sort((m1, m2) -> m1.getMoment().compareTo(m2.getMoment()));
                            }
                        }
                    }
                }
                if (wasNewMessage) runOnUiThread(this::showChatMessages);
            }
            else {
                Log.d("parseChatMessages", "Content has no 'data' " + loadedContent);
            }
        }
        catch (JSONException ex) {
                Log.d("parseChatMessages", ex.getMessage());
        }
    }
    private void showChatMessages() {
        Drawable otherBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.chat_msg_bg_other
        );
        Drawable myBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.chat_msg_bg_my
        );
        Drawable dateBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.chat_msg_date
        );
        boolean wasNesMessage = false;
        synchronized (chatMessages) {
            for (ChatMessage message : chatMessages) {
                if (message.getView() != null) { // данное сообщение уже отображается
                    continue; // пропускаем его
                }

                // tvMessageDate - дата отправки сообщения
                TextView tvMessageDate = new TextView(ChatActivity.this);
                tvMessageDate.setBackground(dateBg);
                tvMessageDate.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams dateParams = (LinearLayout.LayoutParams) tvMessageDate.getLayoutParams();
                if (dateParams == null) {
                    dateParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                dateParams.width = 200;
                dateParams.gravity = Gravity.CENTER; // установка выравнивания по центру
                tvMessageDate.setLayoutParams(dateParams);
                String momentDate = ChatMessage.chatMomentDateFormat.format(message.getMoment());
                tvMessageDate.setText(momentDate);
                chatContainer.addView(tvMessageDate);

                TextView tvMessage = new TextView(ChatActivity.this);
                /* поле Tag традиционно используется для добавления пользовательской информации -
                 * произвольных объектов, связанных с данным представлением (View)
                 * Поместив в это поле ссылку на сообщение, мы можем использовать ее в обработчиках
                 * событий, установленных для представления
                 */
                tvMessage.setTag(message);  // связываем View с ChatMessage
                message.setView(tvMessage); // и наоборот
                // стилизуем: если автор сообщения совпадает с NikName - сообщение "мое"
                if (message.getAuthor().contentEquals(etAuthor.getText())) {
                    String textMy = message.toViewMyMessages();
                    SpannableString spannableString = new SpannableString(textMy);

                    // Установить обычный шрифт для первой строки
                    spannableString.setSpan(
                            new StyleSpan(Typeface.NORMAL),
                            0,
                            textMy.indexOf('\n'),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Установить меньший шрифт и выравнивание по правому краю для второй строки
                    spannableString.setSpan(
                            new RelativeSizeSpan(0.8f),
                            textMy.lastIndexOf('\n') + 1,
                            textMy.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannableString.setSpan(
                            new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                            textMy.lastIndexOf('\n') + 1,
                            textMy.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    tvMessage.setText(spannableString);

                    tvMessage.setBackground(myBg);
                    LinearLayout.LayoutParams myParams = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
                    if (myParams == null) {
                        myParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                    }
                    myParams.gravity = Gravity.RIGHT; // установка правого выравнивания
                    myParams.setMargins(10, 10, 40, 10);
                    tvMessage.setLayoutParams(myParams);
                }
                else { // если автор сообщения не совпадает с NikName - сообщение "чужое"
                    String textOthers = message.toViewOtherMessages();
                    SpannableString spannableString = new SpannableString(textOthers);

                    // Установить жирный шрифт для первой строки
                    spannableString.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            0,
                            textOthers.indexOf('\n'),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Установить обычный шрифт для второй строки
                    spannableString.setSpan(
                            new StyleSpan(Typeface.NORMAL),
                            textOthers.indexOf('\n') + 1,
                            textOthers.lastIndexOf('\n'),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Установить меньший шрифт и выравнивание по правому краю для третьей строки
                    spannableString.setSpan(
                            new RelativeSizeSpan(0.8f),
                            textOthers.lastIndexOf('\n') + 1,
                            textOthers.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannableString.setSpan(
                            new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                            textOthers.lastIndexOf('\n') + 1,
                            textOthers.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    tvMessage.setText(spannableString);

                    tvMessage.setBackground(otherBg);
                    LinearLayout.LayoutParams otherParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    otherParams.setMargins(40, 10, 10, 10);
                    tvMessage.setLayoutParams(otherParams);
                }
                tvMessage.setTextSize(18);
                tvMessage.setMinWidth(300);
                tvMessage.setPadding(20, 10, 20, 10);

                // добавляем сообщение в контейнер
                chatContainer.addView(tvMessage);
                wasNesMessage = true;
            }
            if (wasNesMessage) {
                // даем команду ScrollView прокрутить контент вниз
                svContainer.post(() -> svContainer.fullScroll(View.FOCUS_DOWN));
                // проигрываем звук нового сообщения
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    incomingMessagePlayer.start();
                }
                showNotification();
            }
        }
    }
    private void sendMessageClick( View view ) {
        // TODO: проверить на пустоту автора и сообщение
        new Thread(this::postChatMessage).start();
    }
    private void postChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTxt(etMessage.getText().toString());
        chatMessage.setAuthor(etAuthor.getText().toString());
        try {
            // POST запрос выполняется в несколько этапов
            // 1. Конфигурация подключения
            URL chatUrl = new URL(CHAT_URL);
            HttpURLConnection connection = (HttpURLConnection) chatUrl.openConnection();
            connection.setDoOutput(true); // будет иметь тело (output)
            connection.setDoInput(true);  // будет иметь response
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json"); // заголовки
            connection.setRequestProperty("Accept", "*/*");
            connection.setChunkedStreamingMode(0); // не разделять на чанки (фрагменты)

            // 2. Заполняем тело запроса
            OutputStream body = connection.getOutputStream();
            body.write(chatMessage.toJsonString().getBytes());
            body.close();

            // 3. Получаем ответ
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                Log.e("postChatMessage", "responseCode = " + responseCode);
                return;
            }
            InputStream response = connection.getInputStream();
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = response.read(chunk)) != -1) {
                byteBuilder.write(chunk, 0, len);
            }
            String responseText = byteBuilder.toString(); // "status": "success","data": "Create OK"
            Log.d("postChatMessage", responseText); // TODO: проверить статус ответа

            // освобождаем использованные ресурсы
            byteBuilder.close();
            response.close();
            connection.disconnect();

            // запускаем получение сообщений
            new Thread(this::getChatMessages).start();
        }
        catch (Exception ex) {
            Log.e("postChatMessage", ex.getMessage());
        }
    }

    private void showNotification() {
        /*
        Notification - системное уведомление, остающееся в устройстве
        Использование уведомлений изменялось с разными API:
        В новых версиях добавилось понятие канала уведомлений, в старых его не было
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Регистрация канала уведомлений. Нужны названия канала, его описание, а также
            // идентификатор
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.chat_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.chat_channel_description));
            /*
            Канал регистрируется один раз, после регистрации изменять настройки нельзя
             */
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // Формирование и отправка уведомления
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(ChatActivity.this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.btn_star_big_on)
                        .setContentTitle("Chat")
                        .setContentText("New incoming message")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = notificationBuilder.build();

        NotificationManagerCompat managerCompat = // уведомление - системное, manager - посредник между приложением и системой
                NotificationManagerCompat.from(this);
        // managerCompat.notify(105, notification); // Missing permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверяем наличие разрешений на отправку уведомлений
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Запускаем диалог запроса разрешения
                ActivityCompat.requestPermissions(
                        ChatActivity.this,
                        new String[] {
                                android.Manifest.permission.POST_NOTIFICATIONS
                        },
                        POST_NOTIFICATION_REQUEST_CODE // код запроса - будет содержаться в ответе пользователя
                );
                // диалог запускается асинхронно, когда он закроется активность получит
                // событие onRequestPermissionsResult, обработчик которого нужно перегрузить в данной активности
                return; // предотвратит отправку сообщения если нет разрешения
            }
        }
        managerCompat.notify(
                105, // это значение будет передано в обратной связи от уведомления
                notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATION_REQUEST_CODE) {
            // Обработка результата ответа пользователя
            // String[] permissions - запрошенные разрешения
            // int[] grandResults - результаты от пользователя
        }
    }

    /**
     * ORM for Chat API
     */
    private static class ChatMessage {
        private UUID id;
        private String author;
        private String txt;
        private Date moment;
        private UUID idReply;
        private String replyPreview;
        //////////////////////////////
        private View view; // представление (View), отображающее данное сообщение

        private static final SimpleDateFormat chatMomentFormat = // "Apr 19, 2023 4:41:35 PM"
                new SimpleDateFormat("MMM dd, yyyy KK:mm:ss a", Locale.US);

        private static final SimpleDateFormat chatMomentDateFormat = // "Apr 19"
                new SimpleDateFormat("MMM dd", Locale.US);

        private static final SimpleDateFormat chatMomentTimeFormat = // "18:44"
                new SimpleDateFormat("HH:mm");

        public ChatMessage() {
        }

        public ChatMessage(JSONObject jsonObject) throws JSONException {
            this.setId(UUID.fromString(jsonObject.getString("id")));
            this.setAuthor(jsonObject.getString("author"));
            this.setTxt(jsonObject.getString("txt"));
            try {
                this.setMoment(chatMomentFormat.parse(jsonObject.getString("moment")));
            }
            catch (ParseException ex) {
                throw new JSONException("Moment parse error: " + ex.getMessage());
            }
            // Optional fields
            if (jsonObject.has("idReply")) {
                this.setIdReply(UUID.fromString(jsonObject.getString("idReply")));
            }
            if (jsonObject.has("replyPreview")) {
                this.setReplyPreview(jsonObject.getString("replyPreview"));
            }
        }

        public String toJsonString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("{\"author\": \"%s\", \"txt\": \"%s\"", this.getAuthor(), this.getTxt()));
            if (idReply != null) {
                sb.append(String.format(", \"idReply\": \"%s\"", this.getIdReply()));
            }
            sb.append("}");
            return sb.toString();
        }

        public String toViewOtherMessages() {
            String momentTime = ChatMessage.chatMomentTimeFormat.format(this.getMoment());
            return String.format("%s\n%s\n%s", this.getAuthor(), this.getTxt(), momentTime);
        }

        public String toViewMyMessages() {
            String momentTime = ChatMessage.chatMomentTimeFormat.format(this.getMoment());
            return String.format("%s\n%s", this.getTxt(), momentTime);
        }

        // region Accessors
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public Date getMoment() {
            return moment;
        }

        public void setMoment(Date moment) {
            this.moment = moment;
        }

        public UUID getIdReply() {
            return idReply;
        }

        public void setIdReply(UUID idReply) {
            this.idReply = idReply;
        }

        public String getReplyPreview() {
            return replyPreview;
        }

        public void setReplyPreview(String replyPreview) {
            this.replyPreview = replyPreview;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        // endregion

        /*
      "id": "0c9d3a4c-ded1-11ed-a079-3a2520158311",
      "author": "John Smith",
      "txt": "Классный ник",
      "moment": "Apr 19, 2023 4:41:35 PM",
      "idReply": "4526cc3e-dc49-11ed-88bb-4ad81b26d4d9",
      "replyPreview": "azaza"
         */
    }
}
/*
Работа со звуками (проигрывание звукового файла)
1. Ресурс - относится к ресурсам без категории - "raw"
 - создаем в ресурсах папку "raw"
 - копируем/переносим в нее звуковой файл
2. Объект
 - private MediaPlayer incomingMessagePlayer;
 - incomingMessagePlayer = MediaPlayer.create(this, R.raw.sound_1);
3. Использование
 - incomingMessagePlayer.start();
 - Контроль беззвучных режимов устройства:
 AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
     incomingMessagePlayer.start();
 }
 */
/*
Работа с Интернет
java.net.URL - основной класс (аналог File для файлов)
URL chatUrl = new URL( CHAT_URL ) ; - создание объекта, подключений не устанавливается
chatUrl.openStream() - подключение + поток данных (stream)

android.os.NetworkOnMainThreadException - исключение (без текста сообщения)
выбрасывается если соединение открывается из основного (UI) потока
Работа с сетью должна быть асинхронной

Стартуем в отдельном потоке -
java.lang.SecurityException: Permission denied (missing INTERNET permission?)
Добавляем в манифест
<uses-permission android:name="android.permission.INTERNET"/>

Следствие того что работа с сетью должна быть в отдельном потоке,
является ошибка доступа к UI элементам из другого потока (setText)
Поэтому финальные задачи по отображению желательно переместить в
отдельный метод, который вызывать методом  runOnUiThread( this::showChatMessages ) ;
runOnUiThread( () -> setText )

UI -X- URL             access UI from other thread
                       |
     URL - GET - JSON -X- show(setText)
UI /
                       runOnUiThread
     URL - GET - JSON  |
UI /                  \  show(setText)

                / JSON
     URL - GET          \ runOnUiThread
UI /                     \  show(setText)
 */