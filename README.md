# VDX - better error messages for XML validation

Provides more helpful output when reading XML based on
validation/parse exceptions. Currently only used by [WildFly](http://wildfly.org)
and [JBoss EAP](https://developers.redhat.com/products/eap/overview/).

## Sample Output

With WildFly, instead of seeing:

```
09:40:13,124 ERROR [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0055: Caught exception during boot: org.jboss.as.controller.persistence.ConfigurationPersistenceException: WFLYCTL0085: Failed to parse configuration
	at org.jboss.as.controller.persistence.XmlConfigurationPersister.load(XmlConfigurationPersister.java:143) [wildfly-controller-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.server.ServerService.boot(ServerService.java:373) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.controller.AbstractControllerService$1.run(AbstractControllerService.java:314) [wildfly-controller-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at java.lang.Thread.run(Thread.java:745) [rt.jar:1.8.0_92]
Caused by: com.ctc.wstx.exc.WstxParsingException: Duplicate attribute 'data-source'.
 at [row,col {unknown-source}]: [125,39]
	at com.ctc.wstx.sr.StreamScanner.constructWfcException(StreamScanner.java:621) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.StreamScanner.throwParseError(StreamScanner.java:491) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.StreamScanner.throwParseError(StreamScanner.java:475) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.AttributeCollector.throwDupAttr(AttributeCollector.java:1173) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.AttributeCollector.resolveNamespaces(AttributeCollector.java:1016) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.InputElementStack.resolveAndValidateElement(InputElementStack.java:509) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.BasicStreamReader.handleStartElem(BasicStreamReader.java:3059) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.BasicStreamReader.nextFromTree(BasicStreamReader.java:2919) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.BasicStreamReader.next(BasicStreamReader.java:1123) [woodstox-core-5.0.3.jar:5.0.3]
	at com.ctc.wstx.sr.BasicStreamReader.nextTag(BasicStreamReader.java:1204) [woodstox-core-5.0.3.jar:5.0.3]
	at org.jboss.staxmapper.XMLExtendedStreamReaderImpl.nextTag(XMLExtendedStreamReaderImpl.java:152) [staxmapper-1.3.0.Final.jar:1.3.0.Final]
	at org.wildfly.extension.batch.jberet.BatchSubsystemParser_1_0.parseJobRepository(BatchSubsystemParser_1_0.java:125)
	at org.wildfly.extension.batch.jberet.BatchSubsystemParser_1_0.readElement(BatchSubsystemParser_1_0.java:103)
	at org.wildfly.extension.batch.jberet.BatchSubsystemParser_1_0.readElement(BatchSubsystemParser_1_0.java:52)
	at org.jboss.staxmapper.XMLMapperImpl.processNested(XMLMapperImpl.java:122) [staxmapper-1.3.0.Final.jar:1.3.0.Final]
	at org.jboss.staxmapper.XMLExtendedStreamReaderImpl.handleAny(XMLExtendedStreamReaderImpl.java:69) [staxmapper-1.3.0.Final.jar:1.3.0.Final]
	at org.jboss.as.server.parsing.StandaloneXml_4.parseServerProfile(StandaloneXml_4.java:531) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.server.parsing.StandaloneXml_4.readServerElement(StandaloneXml_4.java:226) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.server.parsing.StandaloneXml_4.readElement(StandaloneXml_4.java:125) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.server.parsing.StandaloneXml.readElement(StandaloneXml.java:104) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.as.server.parsing.StandaloneXml.readElement(StandaloneXml.java:49) [wildfly-server-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	at org.jboss.staxmapper.XMLMapperImpl.processNested(XMLMapperImpl.java:122) [staxmapper-1.3.0.Final.jar:1.3.0.Final]
	at org.jboss.staxmapper.XMLMapperImpl.parseDocument(XMLMapperImpl.java:76) [staxmapper-1.3.0.Final.jar:1.3.0.Final]
	at org.jboss.as.controller.persistence.XmlConfigurationPersister.load(XmlConfigurationPersister.java:126) [wildfly-controller-3.0.0.Alpha23-SNAPSHOT.jar:3.0.0.Alpha23-SNAPSHOT]
	... 3 more
```

You'll now get:

```
09:42:29,037 ERROR [org.jboss.as.controller] (Controller Boot Thread)

OPVDX001: Validation error in standalone.xml -----------------------------------
|
|  123: <job-repository name="in-memory">
|  124:   <jdbc data-source="foo"
|  125:         data-source="bar"/>
|               ^^^^ 'data-source' can't appear more than once on this element
|
|  126: </job-repository>
|  127: <thread-pool name="batch">
|  128:     <max-threads count="10"/>
|
| A 'data-source' attribute first appears here:
|
|  122: <default-thread-pool name="batch"/>
|  123: <job-repository name="in-memory">
|  124:   <jdbc data-source="foo"
|               ^^^^
|
|  125:         data-source="bar"/>
|  126: </job-repository>
|  127: <thread-pool name="batch">
|
| The primary underlying error message was:
| > Duplicate attribute 'data-source'.
| >  at [row,col {unknown-source}]: [125,39]
|
|-------------------------------------------------------------------------------
```

