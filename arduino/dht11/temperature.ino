#include <DHT.h>;
#define DHTPIN 2
#define DHTTYPE DHT22   // DHT 22  (AM2302)
DHT dht(DHTPIN, DHTTYPE);

//Variables
int chk;
float hum;  //Stores humidity value
float temp; //Stores temperature value

void setup() {                
  // Turn the Serial Protocol ON
  Serial.begin(115200);
  
  // Initialize device.
  dht.begin();
}

void loop() {
    int inByte = Serial.read();
    switch (inByte) {
    case 'T':
      // Return the current (T)emperature
      getTemperature();
      break;
    case 'H':
      // Return the current (H)umidity
      getHumidity();
      break;
  }
}

void getTemperature(){
  temp = dht.readTemperature();
  String value = "";
  value += temp;
  
  char charBuf[value.length()+1];
  value.toCharArray(charBuf, value.length()+1);
  
  Serial.write(charBuf);
}

void getHumidity(){
  hum = dht.readHumidity();
  String value = "";
  value += hum;
  
  char charBuf[value.length()+1];
  value.toCharArray(charBuf, value.length()+1);
  
  Serial.write(charBuf);
}
