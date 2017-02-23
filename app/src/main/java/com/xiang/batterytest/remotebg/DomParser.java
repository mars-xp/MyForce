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
						if (nodeName.equals("manufacturer")) {
							PhoneType.getInstance().m_manufacturer = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("mode")) {
							PhoneType.getInstance().m_mode = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("release")) {
							PhoneType.getInstance().m_release = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("sdk")) {
							PhoneType.getInstance().m_sdk = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("package_wait")) {
							PhoneType.getInstance().m_pkgwaitsecond = Integer.valueOf(property.getFirstChild().getNodeValue());
						} else if (nodeName.equals("uab_sdk_ver")) {
							PhoneType.getInstance().m_uabsdkver = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("slice")) {
							PhoneType.getInstance().m_slicemillisecond = Integer.valueOf(property.getFirstChild().getNodeValue());
						} else if (nodeName.equals("match_type")) {
							PhoneType.getInstance().m_matchtype = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("message_type")) {
							PhoneType.getInstance().m_messagetype = Integer.valueOf(property.getFirstChild().getNodeValue());
						} else if (nodeName.equals("action_wait")) {
							PhoneType.getInstance().m_actionwaitmillisecond = Integer.valueOf(property.getFirstChild().getNodeValue());
						}
					}
					if(PhoneType.getInstance().m_matchtype.equalsIgnoreCase("AndroidM") && Build.VERSION.SDK_INT > 22){
						find = true;
					}
					if (PhoneType.getInstance().m_matchtype.equalsIgnoreCase("ALL")
							&& android.os.Build.MANUFACTURER.equalsIgnoreCase(PhoneType.getInstance().m_manufacturer)) {
						find = true;
					}
					if (BuildProperties.isMIUI()
							&& PhoneType.getInstance().m_manufacturer.equalsIgnoreCase("Xiaomi")) {
						find = true;
					}
					if (find == false) {
						if ((android.os.Build.MODEL.equalsIgnoreCase(PhoneType.getInstance().m_mode)
                                || PhoneType.getInstance().m_mode.contains("|||" + android.os.Build.MODEL + "|||"))
								&& android.os.Build.MANUFACTURER
										.equalsIgnoreCase(PhoneType.getInstance().m_manufacturer)
								&& android.os.Build.VERSION.RELEASE
										.equalsIgnoreCase(PhoneType.getInstance().m_release)
								&& android.os.Build.VERSION.SDK
										.equalsIgnoreCase(PhoneType.getInstance().m_sdk)
								&& PhoneType.getInstance().UAB_SDK_VERSION.equalsIgnoreCase(PhoneType.getInstance().m_uabsdkver)) {
							find = true;
						} else if (PhoneType.getInstance().m_manufacturer.equalsIgnoreCase("ALL")) {
							find = true;
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
						if (itemsForceStop.getLength() > i){
							Node item1 = itemsForceStop.item(i);
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
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
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
											} else {
												// error
											}
										}
										if(PhoneType.getInstance().m_asForceStopList != null){
											PhoneType.getInstance().m_asForceStopList.add(actionStep);
										}
									}
								}
							}
						}
					}
					if (itemsNotifiGet != null) {
						if (itemsNotifiGet.getLength() > i)						{
							Node item1 = itemsNotifiGet.item(i);
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
									String nodeName = property.getNodeName();
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
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
											} else {
												// error
											}
										}
									}
                                    if(PhoneType.getInstance().m_asNotifiGetList != null){
                                        PhoneType.getInstance().m_asNotifiGetList.add(actionStep);
                                    }
								}
							}
						}
					}
					if (itemsNotifiClick != null) {
						if (itemsNotifiClick.getLength() > i)						{
							Node item1 = itemsNotifiClick.item(i);
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
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
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
											} else {
												// error
											}
										}
                                        if(PhoneType.getInstance().m_asNotifiClickList != null){
                                            PhoneType.getInstance().m_asNotifiClickList.add(actionStep);
                                        }
									}
								}
							}
						}
					}
					if (itemsClearCatch != null) {
						if (itemsClearCatch.getLength() > i){
							Node item1 = itemsClearCatch.item(i);
							NodeList properties1 = item1.getChildNodes();
							if (properties1 != null) {
								for (int j = 0; j < properties1.getLength(); j++) {
									Node property = properties1.item(j);
									if (property.hasChildNodes() == false) {
										continue;
									}
									ActionStep actionStep = new ActionStep();
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
											} else if (stepName
													.equalsIgnoreCase("ACTION")) {
												actionStep.m_asActionName = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTYPE")) {
												actionStep.m_asElementType = step
														.getFirstChild()
														.getNodeValue();
											} else if (stepName
													.equalsIgnoreCase("ELEMENTTEXT")) {
												actionStep.m_asElementText = step
														.getFirstChild()
														.getNodeValue();
											} else {
												// error
											}
										}
										if(PhoneType.getInstance().m_asClearCatchList != null){
											PhoneType.getInstance().m_asClearCatchList.add(actionStep);
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			find = false;
		}
		if (find == false) {
			if(PhoneType.getInstance().m_asClearCatchList != null){
				PhoneType.getInstance().m_asClearCatchList.clear();
			}
			if(PhoneType.getInstance().m_asForceStopList != null){
				PhoneType.getInstance().m_asForceStopList.clear();
			}
            if(PhoneType.getInstance().m_asNotifiClickList != null){
                PhoneType.getInstance().m_asNotifiClickList.clear();
            }
            if(PhoneType.getInstance().m_asNotifiGetList != null){
                PhoneType.getInstance().m_asNotifiGetList.clear();
            }
		}
		return find;
	}
}
