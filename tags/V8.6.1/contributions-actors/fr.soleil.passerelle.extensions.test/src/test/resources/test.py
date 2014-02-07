if __name__ == "__main__":
	print "in the main"
else:
	import com.isencia.passerelle.message.MessageFactory as MessageFactory
	import java.lang.Double as Double
	import java.lang.reflect.Array as Array
	import com.isencia.passerelle.message.ManagedMessage as ManagedMessage


	inputMessage = container.getInputMessage(0)
	print "nr input " + str(container.getNrInputs())
	print "------------Message0-------------------------\n"
	print inputMessage.getBodyContent()
	print "-------------------------------------\n"
	#print "float value "
	#print float(inputMessage.getBodyContent())
	#print "------------------------------------\n"

	inputMessage = container.getInputMessage(1)
	print "------------Message1-------------------------\n"
	print inputMessage.getBodyContent()
	print "-------------------------------------\n"
	#print "float value "
	#print float(inputMessage.getBodyContent())
	#print "-----------Message0-----------------\n"
	
