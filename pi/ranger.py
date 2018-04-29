# Libraries
import RPi.GPIO as GPIO
import time
import paho.mqtt.publish as publish

# GPIO Mode (BOARD / BCM)
print("set mode")
GPIO.setmode(GPIO.BCM)

# set GPIO Pins
GPIO_TRIGGER = 19
GPIO_ECHO = 26

# mqtt constants
MQTT_SERVER = "10.211.1.127"
MQTT_PATH = "door_distance"

# set GPIO direction (IN / OUT)
print("set trigger out")
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
print("set echo in")
GPIO.setup(GPIO_ECHO, GPIO.IN)


def sendDistance(dist):
	dist = dist * 0.3937
	msg = "distance: " + str(round(dist,2)) + " inches"
	print("mqtt publishes: " + msg)
	publish.single(MQTT_PATH, msg, hostname=MQTT_SERVER)
	print ('Published:' + msg)

def distance():
	GPIO.output(GPIO_TRIGGER, False)
	time.sleep(0.02)
	print("distance running")
	# set Trigger to HIGH
	GPIO.output(GPIO_TRIGGER, True)

	# set Trigger after 0.01ms to LOW
	time.sleep(0.00001)
	GPIO.output(GPIO_TRIGGER, False)

	StartTime = time.time()
	StopTime = time.time()

	# save StartTime
	while GPIO.input(GPIO_ECHO) == 0:
		StartTime = time.time()

	# save time of arrival
	while GPIO.input(GPIO_ECHO) == 1:
		StopTime = time.time()

	# time difference between start and arrival
	TimeElapsed = StopTime - StartTime
	# multiply with the sonic speed (34300 cm/s)
	# and divide by 2, because there and back
	distance = (TimeElapsed * 34300) / 2

	return distance


if __name__ == '__main__':
	try:
		while True:
			dist = distance()
			#print ("Measured Distance = %.1f cm" % dist)
			sendDistance(dist)
			time.sleep(2)

	# Reset by pressing CTRL + C
	except KeyboardInterrupt:
		print("Measurement stopped by User")
		GPIO.cleanup()
