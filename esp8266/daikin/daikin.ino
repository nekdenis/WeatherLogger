#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ir_Daikin.h>

const char* ssid = "wifi-Name";
const char* password = "pass";
const char* mqtt_server = "192.168.2.111";
const int mqtt_port = 1883;
const char* mqtt_user = "testuser";
const char* mqtt_pass = "passwd";

const int sleepTimeS = 60*10;

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = -1;

IRDaikinESP daikinir(D2);

void turnOnDaikin() {
    daikinir.setTemp(25);
    daikinir.setFan(2);
    daikinir.setPower(1);
    daikinir.setPowerful(0);
    daikinir.setQuiet(0);
    daikinir.setSwingHorizontal(0);
    daikinir.setSwingVertical(0);
    daikinir.setMode(DAIKIN_COOL);
    daikinir.send();
}

void turnOffDaikin() {
    daikinir.setTemp(25);
    daikinir.setFan(2);
    daikinir.setPower(0);
    daikinir.setPowerful(0);
    daikinir.setQuiet(0);
    daikinir.setSwingHorizontal(0);
    daikinir.setSwingVertical(0);
    daikinir.setMode(DAIKIN_COOL);
    daikinir.send();
}

void setup_wifi() {

  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  Serial.print("at ");
  Serial.println(millis());

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("at ");
  Serial.println(millis());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  if ((char)payload[0] == '1') {
//    digitalWrite(BUILTIN_LED, LOW);
    if(value!=1){
      turnOnDaikin();
    }
    value = 1;
  } else {
//    digitalWrite(BUILTIN_LED, HIGH);
    if(value!=0){
      turnOffDaikin();
    }
    value = 0;
  }

}

void reconnect() {

  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");

    if (client.connect("kitchenDaikin", mqtt_user, mqtt_pass)) {
      Serial.println("connected");

      client.publish("kitchen_response", "hello world");

      client.subscribe("kitchen_control");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");

      delay(5000);
    }
  }
}

void setup() {
  daikinir.begin();
  pinMode(BUILTIN_LED, OUTPUT);
  digitalWrite(BUILTIN_LED, LOW);
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
  digitalWrite(BUILTIN_LED, HIGH);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }else{
    client.loop();
    client.publish("kitchen_response", "I'm alive");
    Serial.println("ESP8266 in sleep mode");
    ESP.deepSleep(sleepTimeS * 1000000);
  }
}
