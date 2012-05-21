# SCRIPT START

import be.isencia.passerelle.message.MessageFactory as MessageFactory
import java.lang.Double as Double
import java.lang.reflect.Array as Array
import be.isencia.passerelle.message.ManagedMessage as ManagedMessage

# get content of port 0
message = container.getInputMessage(0)
messageBody = message.getBodyContent()
print "Message: ", messageBody

#process content of input
test = range(messageBody.__len__())
test[0] = 12
print test
test[0] = Double.parseDouble(messageBody[0])

j=0
while j < messageBody.__len__():
	test[j] = Double.parseDouble(messageBody[j])
	j = j+1
print test
i=0
while i < messageBody.__len__():
	test[i] = test[i]+1
	
	i = i+1
	
array(test, Double)
print test
#put result in a passerelle message
resultMessage = MessageFactory.getInstance().copyMessage(message)
resultMessage.setBodyContent(10, ManagedMessage.objectContentType) 

#output result on port 0
container.setOutputSpec(0,resultMessage)


#SCRIPT END
