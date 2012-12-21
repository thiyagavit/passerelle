Extracting com.isencia.passerelle.domain.ProcessDirector extensions
===================================================================

- create Passerelle ProcessThread per actor
- register ErrorCollectors
- maintain ErrorControlStrategy
- maintain ExecutionControlStrategy
- maintain ExecutionPrePostProcessor
- register FiringEventListeners

Extracting com.isencia.passerelle.domain.cap.Director extensions
================================================================

- create Passerelle BlockingQueueReceiver
- mock Mode parameter
- expert Mode parameter (check what Ptolemy has for this)
- validate iteration parameter
- validate initialization parameter
- maintain central Quartz scheduler per flow


Breaking old APIs 
=================

- removed actor.V3 package
- PortMode.PULL & PUSH extended with boolean indicator about blocking or non_blocking
- AsyncModelExecutor, SyncModelExecutor, ModelExecutor cleaned up to one merged ModelExecutor
- removed MessageAndPort, MessageFLowContext, MultiMessageFLowContext
- on Actor base class :
-- removed sendOutputMsg(MessageAndPort) and SendOutputMsgs(MessageAndPort[])
- removed portIndex parameter in sendOutputMsg() and MessageOutputContext