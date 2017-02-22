package com.xiang.batterytest.remotebg;

import android.os.Build;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomParser {
	public boolean parse(InputStream is) {
		boolean find = false;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			Element rootElement = (Element) doc.getDocumentElement();
			NodeList itemsBase = rootElement.getElementsByTagName("BASEINFO");
			PhoneType phoneType = PhoneType.getInstance();
			if (itemsBase != null) {
				for (int i = 0; i < itemsBase.getLength(); i++) {
					if (find) {
						break;
					}
					Node item = itemsBase.item(i);
					NodeList properties = item.getChildNodes();
					for (int j = 0; j < properties.getLength(); j++) {
						Node property = properties.item(j);
						String nodeName = property.getNodeName();
						if (nodeName.equals("id")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
						} else if (nodeName.equals("manufacturer")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_manufacturer = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("mode")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_mode = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("release")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_release = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("sdk")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_sdk = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("package_wait")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_pkgwaitsecond = Integer
									.valueOf(property.getFirstChild()
											.getNodeValue());
						} else if (nodeName.equals("interval")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							// phoneType.m_intervalmillisecond =
							// Integer.valueOf(property.getFirstChild().getNodeValue());
						} else if (nodeName.equals("uab_sdk_ver")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_uabsdkver = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("slice")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_slicemillisecond = Integer
									.valueOf(property.getFirstChild()
											.getNodeValue());
						} else if (nodeName.equals("match_type")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_matchtype = property.getFirstChild()
									.getNodeValue();
						} else if (nodeName.equals("message_type")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_messagetype = Integer.valueOf(property
									.getFirstChild().getNodeValue());
						} else if (nodeName.equals("action_wait")) {
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// (property.getFirstChild().getNodeValue()));
							phoneType.m_actionwaitmillisecond = Integer
									.valueOf(property.getFirstChild()
											.getNodeValue());
						}
					}
					if(phoneType.m_matchtype.equalsIgnoreCase("AndroidM") && Build.VERSION.SDK_INT > 22){
						find = true;
					}

					if (phoneType.m_matchtype.equalsIgnoreCase("ALL")
							&& android.os.Build.MANUFACTURER
									.equalsIgnoreCase(phoneType.m_manufacturer)) {
						find = true;
					}

					if (BuildProperties.isMIUI()
							&& phoneType.m_manufacturer
									.equalsIgnoreCase("Xiaomi")) {
						find = true;
					}

					if (find == false) {
						if ((android.os.Build.MODEL
								.equalsIgnoreCase(phoneType.m_mode) || phoneType.m_mode
								.contains("|||" + android.os.Build.MODEL
										+ "|||"))
								&& android.os.Build.MANUFACTURER
										.equalsIgnoreCase(phoneType.m_manufacturer)
								&& android.os.Build.VERSION.RELEASE
										.equalsIgnoreCase(phoneType.m_release)
								&& android.os.Build.VERSION.SDK
										.equalsIgnoreCase(phoneType.m_sdk)
								&& PhoneType.UAB_SDK_VERSION
										.equalsIgnoreCase(phoneType.m_uabsdkver)) {
							find = true;
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// "Find");
						} else if (phoneType.m_manufacturer
								.equalsIgnoreCase("ALL")) {
							find = true;
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// "ALL Find");
						} else {
							continue;
						}
					}
					NodeList itemsForceStop = rootElement
							.getElementsByTagName("FORCESTOP");
					NodeList itemsNotifiClick = rootElement
							.getElementsByTagName("NOTIFICLICK");
					NodeList itemsNotifiGet = rootElement
							.getElementsByTagName("NOTIFIGET");
					NodeList itemsClearCatch = rootElement
							.getElementsByTagName("CLEARCATCH");
					if (itemsForceStop != null) {
						if (itemsForceStop.getLength() > i)

						// for (int i1 = 0; i1 < itemsForceStop.getLength();
						// i1++)
						{
							Node item1 = itemsForceStop.item(i);
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// item1.getNodeName());
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
								// Integer.toString(properties.getLength()));
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
									// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
									// nodeName +
									// Integer.toString(properties.getLength()));
									NodeList stepList = property
											.getChildNodes();
									if (stepList != null) {
										for (int k = 0; k < stepList
												.getLength(); k++) {
											Node step = stepList.item(k);
											String stepName = step
													.getNodeName();
											if (stepName
													.equalsIgnoreCase("ACTIVITY")) {
												actionStep.m_asActivityName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActivityName);
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActionName);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementType);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementText);
											} else {
												// error
											}
										}
										phoneType.m_asForceStopList
												.add(actionStep);
									}
								}
							}
						}
					}
					if (itemsNotifiGet != null) {
						if (itemsNotifiGet.getLength() > i)
						// for (int i1 = 0; i1 < itemsNotifiGet.getLength();
						// i1++)
						{
							Node item1 = itemsNotifiGet.item(i);
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// item1.getNodeName() + "  " +
							// itemsNotifiGet.getLength() );
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
								// Integer.toString(properties.getLength()));
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
									String nodeName = property.getNodeName();
									// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
									// nodeName +
									// Integer.toString(properties1.getLength()));
									NodeList stepList = property
											.getChildNodes();
									if (stepList != null) {
										for (int k = 0; k < stepList
												.getLength(); k++) {
											Node step = stepList.item(k);
											String stepName = step
													.getNodeName();
											if (stepName
													.equalsIgnoreCase("ACTIVITY")) {
												actionStep.m_asActivityName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActivityName);
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActionName);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementType);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementText);
											} else {
												// error
											}
										}
									}
									phoneType.m_asNotifiGetList.add(actionStep);
								}
							}
						}
					}
					if (itemsNotifiClick != null) {
						if (itemsNotifiClick.getLength() > i)
						// for (int i1 = 0; i1 < itemsNotifiClick.getLength();
						// i1++)
						{
							Node item1 = itemsNotifiClick.item(i);
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// item1.getNodeName());
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
								// Integer.toString(properties.getLength()));
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
									// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
									// nodeName +
									// Integer.toString(properties.getLength()));
									NodeList stepList = property
											.getChildNodes();
									if (stepList != null) {
										for (int k = 0; k < stepList
												.getLength(); k++) {
											Node step = stepList.item(k);
											String stepName = step
													.getNodeName();
											if (stepName
													.equalsIgnoreCase("ACTIVITY")) {
												actionStep.m_asActivityName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActivityName);
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActionName);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementType);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementText);
											} else {
												// error
											}
										}
										phoneType.m_asNotifiClickList
												.add(actionStep);
									}
								}
							}
						}
					}
					if (itemsClearCatch != null) {
						if (itemsClearCatch.getLength() > i)
						// for (int i1 = 0; i1 < itemsClearCatch.getLength();
						// i1++)
						{
							Node item1 = itemsClearCatch.item(i);
							// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
							// item1.getNodeName());
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
								// Integer.toString(properties.getLength()));
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
									// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
									// nodeName +
									// Integer.toString(properties.getLength()));
									NodeList stepList = property
											.getChildNodes();
									if (stepList != null) {
										for (int k = 0; k < stepList
												.getLength(); k++) {
											Node step = stepList.item(k);
											String stepName = step
													.getNodeName();
											if (stepName
													.equalsIgnoreCase("ACTIVITY")) {
												actionStep.m_asActivityName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActivityName);
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asActionName);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementType);
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
												// PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
												// actionStep.m_asElementText);
											} else {
												// error
											}
										}
										phoneType.m_asClearCatchList
												.add(actionStep);
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// PhoneType.logInfo(PhoneType.TYPE_LOGINFO, e.toString());
//			PhoneType.m_errMsg = new String();
			PhoneType.getInstance().m_errMsg = "[DomParser.parse] Exception: " + e.toString();
			find = false;
		}
		if (find == false) {
			PhoneType.getInstance().m_asClearCatchList.clear();
			PhoneType.getInstance().m_asFloatWindowList.clear();
			PhoneType.getInstance().m_asForceStopList.clear();
			PhoneType.getInstance().m_asNotifiClickList.clear();
			PhoneType.getInstance().m_asRateFlowList.clear();
		}
		return find;
	}
}
