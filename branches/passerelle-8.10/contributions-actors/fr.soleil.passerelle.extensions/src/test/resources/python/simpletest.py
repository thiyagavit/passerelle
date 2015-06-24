import be.isencia.passerelle.message.MessageFlowElement as MessageFlowElement
import be.isencia.passerelle.message.MessageFactory as MessageFactory
import be.isencia.passerelle.message.ManagedMessage as ManagedMessage
import be.isencia.passerelle.core.PortFactory as PortFactory

#input2 = PortFactory.getInstance().createInputPort(container,"titi")
#output2 = PortFactory.getInstance().createOutputPort(container,"toto")

message = container.getInputMessage()

outputvalue = message.getBodyContent()

print "value:" + outputvalue

message.setBodyContent(outputvalue, ManagedMessage.objectContentType)

container.addOutputSpec(0,message)

