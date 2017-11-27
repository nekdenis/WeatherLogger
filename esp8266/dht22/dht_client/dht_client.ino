#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include "DHT.h"
//#include <MQTTClient.h>

#define DHTTYPE DHT22
#define DHTPIN 4

#define FORCE_DEEPSLEEP

const char* ssid = "gddeurope";
const char* password = "howicode";
const char* mqtt_server = "10.200.2.1";
const int mqtt_port = 1883;
const char* mqtt_user = "testuser";
const char* mqtt_pass = "passwd";
char msg[12];
char tem[7];
char hum[7];

DHT dht(DHTPIN, DHTTYPE);
WiFiClient espClient;
PubSubClient client(espClient);

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
}

void reconnect() {

  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");

    if (client.connect("roomTemp", mqtt_user, mqtt_pass)) {
      Serial.println("connected");
      client.subscribe("room/temp_control");
      digitalWrite(BUILTIN_LED, LOW);
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");

      delay(5000);
    }
  }
}

void setup(void){
  dht.begin();
  pinMode(BUILTIN_LED, OUTPUT);
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

void loop(void){
  if (!client.connected()) {
    reconnect();
  }else{
    delay(1000*10);
    float h = dht.readHumidity();
    float t = dht.readTemperature();
    String value = "temp = ";
    value += t;
    value += "  humidity = ";
    value += h;
    value += "";
    Serial.println(value);
    dtostrf(dht.readHumidity(), 5, 2, hum);
    dtostrf(dht.readTemperature(), 5 , 2, tem);
    snprintf(msg, sizeof(msg), "%s|%s", tem, hum);
    Serial.println(msg);
    client.publish("room/temperature", msg);
    client.loop();
  }
}

