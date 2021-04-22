int getRotation(int rotation) {
  imu::Quaternion quat = bno.getQuat();
  imu::Vector<3> accel = bno.getVector(Adafruit_BNO055::VECTOR_ACCELEROMETER);
  imu::Vector<3> gyros = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);

  double startQuat, startQuatW, startGyros;
  double previousQuat, previousQuatW, previousGyros;
  double currentQuat, currentQuatW, currentGyros;
  double median[10], median2[10];
  double medianW[10], medianW2[10];
  double medianGyro[10], medianGyro2[10];
  boolean verify;
  
  int halfRotation = 0;
  int loopCount = 0;
  int counter = 0;

  for (int i = 0; i < 10; i++)
  {
      imu::Quaternion quat = bno.getQuat();
      median[i] = quat.x();
      medianW[i] = quat.w();
      imu::Vector<3> gyros = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
      medianGyro[i] = gyros.y();
  }
  
  startQuat = getMedian(median, 10);
  startQuatW = getMedian(medianW, 10);
  startGyros = getMedian(medianGyro, 10);

  while(1)
  {
    while (halfRotation == 0 && loopCount < 4)
    {
      for (int i = 0; i < 10; i++)
      {
        imu::Quaternion quat = bno.getQuat();
        median[i] = quat.x();
        medianW[i] = quat.w();
        imu::Vector<3> gyros = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
        medianGyro[i] = gyros.y();
      }
      
      previousQuat = getMedian(median, 10);
      previousQuatW = getMedian(medianW, 10);
      previousGyros = getMedian(medianGyro, 10);
  
      for (int i = 0; i < 10; i++)
      {
        imu::Quaternion quat = bno.getQuat();
        median2[i] = quat.x();
        medianW2[i] = quat.w();
        imu::Vector<3> gyros = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
        medianGyro2[i] = gyros.y();
      }

      currentQuat = getMedian(median2, 10);
      currentQuatW = getMedian(medianW2, 10);
      currentGyros = getMedian(medianGyro2, 10);

      if (startQuatW < 0)
      {
        if ((currentQuatW >= previousQuatW) && (currentQuatW >= startQuatW))
        {
          verify = true;
        }
        else
        {
          verify = false;
        }
      }

      else
      {
        if ((currentQuatW <= previousQuatW) && (currentQuatW <= startQuatW))
        {
          verify = true;
        }
        else
        {
          verify = false;
        }
      }
    
      if ((previousQuat > currentQuat) && (currentGyros > previousGyros) && verify)
      {
        if (((startQuat - currentQuat) >= 0.1) && (abs(startGyros - currentGyros) >= 80))
        {
          halfRotation = 1;
          startQuat = currentQuat;
          startGyros = currentGyros;
          break;
        }
      }

      else
      {
        loopCount++;
      }

    }
    loopCount = 0;
    break;
  }

  if (halfRotation == 1)
  {
    return rotation + 1;
  }

  else
  {
    return rotation;
  }
}
