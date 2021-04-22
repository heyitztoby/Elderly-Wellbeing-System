/*********************************************************************
This class is to obtain values from the BNO055 Sensor and calibration.
*********************************************************************/

int displayCalStatus(void)
{
  /* Get the four calibration values (0..3) */
  /* Any sensor data reporting 0 should be ignored, */
  /* 3 means 'fully calibrated" */
  uint8_t system, gyro, accel, mag;
  system = gyro = accel = mag = 0;
  bno.getCalibration(&system, &gyro, &accel, &mag);

  /* The data should be ignored until the system calibration is > 0 */
  Serial.print("\t");
  if (!system)
  {
    bleuart.print("! ");
  }

  if (gyro == 3 && accel == 3 && mag == 3) {
    bleuart.println(F("Calibrated successfully"));
    return 0;
  }

  else {
  /* Display the individual values */
  bleuart.print("Sys:");
  bleuart.print(system, DEC);
  bleuart.print(" G:");
  bleuart.print(gyro, DEC);
  bleuart.print(" A:");
  bleuart.print(accel, DEC);
  bleuart.print(" M:");
  bleuart.print(mag, DEC);
  bleuart.println("");

  delay(100);

  return 1;
  }

  
}

int displayCalStatus2(void)
{
  /* Get the four calibration values (0..3) */
  /* Any sensor data reporting 0 should be ignored, */
  /* 3 means 'fully calibrated" */
  uint8_t system, gyro, accel, mag;
  system = gyro = accel = mag = 0;
  bno.getCalibration(&system, &gyro, &accel, &mag);

  /* The data should be ignored until the system calibration is > 0 */
  Serial.print("\t");
  if (!system)
  {
    Serial.print("! ");
  }

  if (gyro == 3 && accel == 3 && mag == 3) {
    Serial.println(F("Calibrated successfully"));
    return 0;
  }

  else {
  /* Display the individual values */
  Serial.print("Sys:");
  Serial.print(system, DEC);
  Serial.print(" G:");
  Serial.print(gyro, DEC);
  Serial.print(" A:");
  Serial.print(accel, DEC);
  Serial.print(" M:");
  Serial.print(mag, DEC);
  Serial.println("");

  delay(100);

  return 1;
  }

  
}

void getGyro() {
    imu::Vector<3> gyros = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
//        Serial.print("gX: ");
//        Serial.print(gyros.x());
//        Serial.print(" gY: ");
//        Serial.print(gyros.y());
//        Serial.print(" gZ: ");
//        Serial.print(gyros.z());
noOfRotation = getRotation(noOfRotation);
    Serial.print(" No.: ");
    Serial.print(noOfRotation);
        Serial.println("");
}

void getAccel() {
  imu::Vector<3> accel = bno.getVector(Adafruit_BNO055::VECTOR_ACCELEROMETER);

        Serial.print("aX: ");
        Serial.print(accel.x());
//        Serial.print(" aY: ");
//        Serial.print(accel.y());
//        Serial.print(" aZ: ");
//        Serial.print(accel.z());
//        Serial.print(" No.: ");
//        Serial.print(noOfRotation);
        Serial.println("");

//        noOfRotation = getRotation(noOfRotation);

        
        
}

void getMagnet() {
  imu::Vector<3> mag = bno.getVector(Adafruit_BNO055::VECTOR_MAGNETOMETER);
        Serial.print("mX: ");
        Serial.print(mag.x());
        Serial.print(" mY: ");
        Serial.print(mag.y());
        Serial.print(" mZ: ");
        Serial.print(mag.z());
        Serial.println("");
}

void getQuat() {
    imu::Quaternion quat = bno.getQuat();
    /* Display the quat data */
//    Serial.print(" qX: ");
//    Serial.print(quat.x(), 4);
//    Serial.print(" qY: ");
//    Serial.print(quat.y(), 4);
    Serial.print(" qZ: ");
    Serial.print(quat.z(), 4);
    Serial.print(", qW: ");
    Serial.print(quat.w(), 4);
//    Serial.print(" No.: ");
//    Serial.print(noOfRotation);
    Serial.println("");

//    noOfRotation = getRotation(noOfRotation);
}
