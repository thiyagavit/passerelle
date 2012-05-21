
#import fr.esrf.Tango.* as *
import fr.esrf.Tango.DevState as DevState
import fr.esrf.Tango.DevError as DevError
import fr.esrf.Tango.DevFailed as DevFailed
import fr.esrf.Tango.DevInfo as DevInfo
import fr.esrf.TangoApi.DeviceAttribute as DeviceAttribute
import fr.esrf.TangoApi.DeviceData as DeviceData
import fr.esrf.TangoApi.DeviceProxy as DeviceProxy
import fr.esrf.Tango.AttributeValue as AttributeValue
import java.lang.Double as Double
import java.lang.Integer as Integer
import java.lang.String as String
import java.lang.Long as Long
import be.isencia.passerelle.message.ManagedMessage as ManagedMessage
import fr.soleil.tango.util.AttributeBasicHelper as Attribute
#from java.io import DataInputStream,FileInputStream 
#import os
import sys
import time
import be.isencia.passerelle.message.MessageFlowElement as MessageFlowElement
import be.isencia.passerelle.message.MessageFactory as MessageFactory

#===============================================
# variables by default
#===============================================
# write the name of your Device Server "ScanServer"
scan_device_server_name = "X07MA/acquisition/scanbragg"
scan_1d_front = ""
scan_2d_front = ""

# define the file where data should be stored
path = "c:\\temp\\"
data_file = "monotest_1D"
file_extension = ".dat"

# define the actuactors
actuators = ["tmp/test/luciamono/Energy"]
# define the sensors
sensors = ["tmp/test/luciamono/Energy","tmp/test/luciamono/Thetam"]
	
# define the number of steps for scan 1D or first level of scan 2D
n_steps = 5
# integration time in second
integration_time = 1

# define positions of actuators for scan 1D
start_position_actuators_1D = [2470]
end_position_actuators_1D = [2480]

# define the external timebase ef exist, put "" if internal timebase
timebase = "X07MA/acquisition/scaler"

#number of scans to execute
scan_number = 1

# Select scan type : 1=1D , 2=2D
scan_type = 1

actuators2D = ["tmp/test/luciamono/Energy"]
# define the number of steps for scan 2D (scend level)
n_steps2 = 3
# define positions of actuators for scan 2D
start_position_actuators_2D = [100]
end_position_actuators_2D = [200]

# Has Trajectory : 0: non , 1: oui	
has_trajectory = 0	
# define the trajectory points
#trajectory_points = [point1,point2,...]
file_path = 'D:\eclipse\workspace\passerelle-soleil\sample-models\paramscan.txt'

def parameterScan():
	global scan_device_server_name	
	global scan_1d_front
	global scan_2d_front
	global path 
	global data_file 
	global file_extension 
	global actuators
	global sensors 
	global n_steps 
	global integration_time 
	global start_position_actuators_1D 
	global end_position_actuators_1D 
	global n_steps2 
	global start_position_actuators_2D 
	global end_position_actuators_2D 
	global timebase 
	global scan_number 
	global scan_type 
	global has_trajectory	
	global file_path
	global actuators2D
	#open the file in read only mode
	#file = DataInputStream(FileInputStream("D:\eclipse\workspace\passerelle-soleil\sample-models\paramscan.txt"))
	file =  open(file_path,'r')
	parameters = {}
	for line in file.readlines():
		#print "line ",line
		tokens = line.split('=')
		#print "tokens ",tokens
		if(len(tokens) == 2):
			tok0 = String(tokens[0])
			tok1 = String(tokens[1])
			tok0 = tok0.trim()
			tok1 = tok1.trim()
			parameters[tok0] = tok1
			print tok0,":",tok1
	
	if(parameters.has_key("scan_device_server_name")):
		scan_device_server_name = parameters["scan_device_server_name"]
		
	if(parameters.has_key("scan_1d_front")):
		scan_1d_front = parameters["scan_1d_front"]
		
	if(parameters.has_key("scan_2d_front")):
		scan_2d_front = parameters["scan_2d_front"]
	
	if(parameters.has_key("path")):
		path = parameters["path"]
	
	if(parameters.has_key("data_file")):
		data_file = parameters["data_file"]
	
	if(parameters.has_key("file_extension")):
		file_extension = parameters["file_extension"]
	
	if(parameters.has_key("actuators")):
		actuators = parameters["actuators"].split(",")
		#print  "parameterScan actuators ",parameters["actuators"].split(",")
   	
   	if(parameters.has_key("sensors")):
		sensors = parameters["sensors"].split(",")
  	
  	#if(parameters.has_key("n_steps")):
		#n_steps = Integer.parseInt(parameters["n_steps"])
   	
   	#if(parameters.has_key("integration_time")):
		#integration_time = Double.parseDouble(parameters["integration_time"])
	
	#if(parameters.has_key("start_position_actuators_1D")):	
	#	stringTable = parameters["start_position_actuators_1D"].split(",")
	#	start_position_actuators_1D = []
	#	for i in stringTable:
	#		start_position_actuators_1D.append(Double.parseDouble(i))
   	
   	if(parameters.has_key("end_position_actuators_1D")):
		stringTable = parameters["end_position_actuators_1D"].split(",")
		end_position_actuators_1D = []
		for i in stringTable:
			end_position_actuators_1D.append(Double.parseDouble(i))
	
	if(parameters.has_key("actuators2D")):
		actuators2D = parameters["actuators2D"].split(",")
		#print  "parameterScan actuators ",parameters["actuators"].split(",")
	
	if(parameters.has_key("n_steps2")):
		n_steps2 = Integer.parseInt(parameters["n_steps2"])
	
	if(parameters.has_key("start_position_actuators_2D")):	
		stringTable = parameters["start_position_actuators_2D"].split(",")
		start_position_actuators_2D = []
		for i in stringTable:
			start_position_actuators_2D.append(Double.parseDouble(i))
   	
   	if(parameters.has_key("end_position_actuators_2D")):
		stringTable = parameters["end_position_actuators_2D"].split(",")
		end_position_actuators_2D = []
		for i in stringTable:
			end_position_actuators_2D.append(Double.parseDouble(i))
   	
   	if(parameters.has_key("timebase")):
		timebase = parameters["timebase"]
	
	if(parameters.has_key("scan_number")):
		scan_number = Integer.parseInt(parameters["scan_number"])
   	
   	if(parameters.has_key("scan_type")):
		scan_type = Integer.parseInt(parameters["scan_type"])
   	
   	if(parameters.has_key("has_trajectory")):
		has_trajectory = Integer.parseInt(parameters["has_trajectory"])
	#print "parameters: ",parameters	
#end parameterScan

#===============================================
# Get file path
#===============================================
print "begin"
message = container.getInputMessage(0)
messageBody = message.getBodyContentAsString()

#if inputPortNr == 0:
print "input 0: ",messageBody
file_path = messageBody
	
message = container.getInputMessage(1)
messageBody = Long.parseLong(message.getBodyContentAsString())
#if inputPortNr == 1:
print "input 1: ",messageBody
n_steps = messageBody


message = container.getInputMessage(2)
messageBody = Double.parseDouble(message.getBodyContentAsString())
print "input 2: ",messageBody
integration_time = messageBody

message = container.getInputMessage(3)
messageBody = message.getBodyContentAsString()
print "input 3: ",messageBody
stringTable = messageBody.split(",")
start_position_actuators_1D = []
for i in stringTable:
		start_position_actuators_1D.append(Double.parseDouble(i))
#===============================================
# Get all parameters from file
#===============================================
parameterScan()
		
try :

	#===============================================
	# Scan Server Proxy
	#===============================================
	scan = DeviceProxy(scan_device_server_name)
	print "scan.ping() ",scan.ping()," us"
	#print "scan.Abort()"
	#scan.command_inout("Abort",None)
	
	state = scan.state()
	print "scan.state() ",state
	while state != DevState.ON:
		state = scan.state()
		print "scan state ",state
		time.sleep(1)
		
	#===============================================
	# Write Scan Attribute
	#===============================================
	print "*******configure scan server*********"
	print "n_steps ",n_steps
	da = scan.read_attribute("n_steps")
	da.insert([n_steps])
	scan.write_attribute(da)
	
	print "integration_time ",integration_time
	da = scan.read_attribute("integration_time")
	da.insert([integration_time])
	scan.write_attribute(da)

	print "data_file ",data_file
	da = scan.read_attribute("data_file")
	da.insert(data_file)
	scan.write_attribute(da)

	print "path ",path
	da = scan.read_attribute("path")
	da.insert(path)
	scan.write_attribute(da)

	print "file_extension ",file_extension
	da = scan.read_attribute("file_extension")
	da.insert(file_extension)
	#scan.write_attribute(da)

	print "scan_number:",scan_number
	da = scan.read_attribute("scan_number")
	da.insert(scan_number)
	scan.write_attribute(da)
	
##	attr_val.name = "timebase"
##	attr_val.value = timebase
##	scan.write_attribute(attr_val)

	#===============================================
	# Scan server Command Parameters
	#===============================================
	print "scan.SetActuators", actuators
	dd = DeviceData()
	dd.insert(actuators)
	scan.command_inout("SetActuators",dd)

	print "scan.SetSensors ", sensors
	dd.insert(sensors)
	scan.command_inout("SetSensors",dd)

	#===============================================
	# Trajectory
	#===============================================
	
	#if (has_trajectory == 0): #non trajectored
	print "scan.SetStartPositions", start_position_actuators_1D
	dd.insert(start_position_actuators_1D)
	scan.command_inout("SetStartPositions",dd)
	print "scan.SetEndPositions", end_position_actuators_1D
	dd.insert(end_position_actuators_1D)
	scan.command_inout("SetEndPositions",dd)
	#else:
	#	dd.insert(trajectory_points)
	#	scan.command_inout("SetTrajectory",dd)

	#===============================================
	# Dealing with the 2D case
	#===============================================
	if scan_type == 2:

		print "scan.SetActuators2", actuators2D
		dd.insert(actuators2D)
		scan.command_inout("SetActuators2",dd)

		da = scan.read_attribute("n_steps2")
		da.insert(n_steps2)
		scan.write_attribute(da)
		
		#if (has_trajectory == 0):#non trajectoried
		print "SetStartPositions2", start_position_actuators_2D
		dd.insert(start_position_actuators_2D)
		scan.command_inout("SetStartPositions2",dd)
		print "SetEndPositions2", end_position_actuators_2D
		dd.insert(end_position_actuators_2D)
		scan.command_inout("SetEndPositions2",dd)
#		else:
#			argin=[100,245,
#			       458,569]
#			dd.insert(argin)
#			scan.command_inout("SetTrajectory2",dd)
	else :
		#just for the calculated time
		n_steps2 = 1

	#===============================================
	# Scan Start
	#===============================================
	# Scan Type
	print "****************start scan*****"
	if scan_type == 2:
		print "scan.Scan2D()"
		print "n_steps = ",n_steps	
		print "n_steps2 = ",n_steps2
		scan.command_inout("Scan2D")
		
	else:
		#print "n_steps = ",n_steps
		scan.command_inout("Scan1D")
		
	# Trajectored or not
	if has_trajectory == 1:
		print "Trajectored scan"
	else:
		print "Non Trajectored scan"
		
	# display some usefull info
	#print "integration_time = ",integration_time
	#print "scan_number = ",scan_number
	state = scan.state()
	print "scan state ",state
	
	while state == DevState.MOVING:
		print "======waiting==========="
		# get the real time for this step to be done
		da = scan.read_attribute("step_time")
		print "step_time = " ,da.extractDouble(), "; "

		# check if scan reach the final number of steps
		da = scan.read_attribute("n_steps")
		print "n_steps = " ,da.extractLong(), "; "
		
		if(scan_type == 2):
			da = scan.read_attribute("n_steps2")
			print "n_steps2 = " ,da.extractLong(), "; "
			

		# do a sleep to avoid print taking processor,
		# wait for the next move of the actuators
		time.sleep(integration_time)
		state = scan.state()

	
	print "********Scan finished*********"
	
	if scan_type == 2:
		theorical_time = (n_steps * n_steps2 * integration_time * scan_number)
	else:
		theorical_time = (n_steps * integration_time * scan_number)
	print "Theorical Time = ", theorical_time , " secondes "
		
	# pourcentage de tps mort:
	da = scan.read_attribute("scan_time")
	pourcent = ((da.extractDouble() - theorical_time)/theorical_time)*100
	print "Real Time = ",da.extractDouble()
	print "DeadTime percent: ", pourcent," %"
	
	

	#===============================================
	# Display data
	#===============================================
	#spectrum_sens 
	spectrum_sens=scan.read_attribute("sensors_data")
	print "sensors data value", spectrum_sens.extractDoubleArray()

	#spectrum_act = 0
	spectrum_act=scan.read_attribute("actuators_data")
	print "actuators value", spectrum_act.extractDoubleArray()

	if scan_type == 2:
		#spectrum_act2 = 0
		spectrum_act2=scan.read_attribute("actuators2_data")
		print "actuators2 value", spectrum_act2.extractDoubleArray()

#	if spectrum_sens == spectrum_act:
#		print "Scan_1D Reg Test PASSED"
#	else:
#		print "Scan_1D Reg Test FAILED"

	#output n_steps on output 0
	
	if(scan_type == 1):
		sensor1 = Attribute(scan_1d_front+"/sensor1Data")
		sensor1.read()
		message.setBodyContent(sensor1, ManagedMessage.objectContentType)
		
	else:
		sensor2 = Attribute(scan_2d_front+"/sensor1Data")
		sensor2.read()
		message.setBodyContent(sensor2, ManagedMessage.objectContentType)
	
	container.addOutputSpec(0,message)
	
	sensor3 = Attribute(scan_1d_front+"/sensor2Data")
	sensor3.read()
	message.setBodyContent(sensor3, ManagedMessage.objectContentType)
	container.addOutputSpec(1,message)
	

# EXCEPTIONS	
except DevFailed, instance:
	#exctype , value = sys.exc_info()[:2]
	#print "Failed with exception !", exctype
	for err in instance.errors :
		print "------- ERROR ELEMENT -------"
		print "reason:" , err.reason
		print "description:" , err.desc
		print "origin:" , err.origin
		print "severity:" , err.severity

