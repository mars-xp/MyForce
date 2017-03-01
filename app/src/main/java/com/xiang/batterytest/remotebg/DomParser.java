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
						if(nodeName.equals("id")){
							String vStrId = property.getFirstChild().getNodeValue();
							PhoneType.getInstance().m_id = Integer.valueOf(vStrId);
						}
						else if (nodeName.equals("manufacturer")) {
							PhoneType.getInstance().m_manufacturer = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("mode")) {
							PhoneType.getInstance().m_mode = property.getFirstChild().getNodeValue();
						} else if (nodeName.equals("minsdk")) {
							String vMiniSdk = property.getFirstChild().getNodeValue();
							PhoneType.getInstance().m_minsdk = Integer.valueOf(vMiniSdk);
						} else if (nodeName.equals("maxsdk")) {
							String vMaxSdk = property.getFirstChild().getNodeValue();
							PhoneType.getInstance().m_maxsdk = Integer.valueOf(vMaxSdk);
						} else if (nodeName.equals("uab_sdk_ver")) {
							PhoneType.getInstance().m_uabsdkver = property.getFirstChild().getNodeValue();
						}
					}
					if(PhoneType.getInstance().m_id == 1){
						find = true;
					}
					else{
						if(PhoneType.getInstance().m_manufacturer.equalsIgnoreCase(Build.MANUFACTURER)
								|| (BuildProperties.isMIUI() && PhoneType.getInstance().m_manufacturer.equals("Xiaomi"))){
							if(PhoneType.getInstance().m_mode.equalsIgnoreCase("all")){
								if(PhoneType.getInstance().m_maxsdk == 0){
									find = true;
								}
								else if(Build.VERSION.SDK_INT >= PhoneType.getInstance().m_minsdk
										&& Build.VERSION.SDK_INT <= PhoneType.getInstance().m_maxsdk){
									find = true;
								}
							}
							else if(PhoneType.getInstance().m_mode.contains(Build.MODEL)){
								if(PhoneType.getInstance().m_maxsdk == 0){
									find = true;
								}
								else if(Build.VERSION.SDK_INT >= PhoneType.getInstance().m_minsdk
										&& Build.VERSION.SDK_INT <= PhoneType.getInstance().m_maxsdk){
									find = true;
								}
							}
						}
					}
					if(!find){
						continue;
					}
					NodeList itemsForceStop = rootElement.getElementsByTagName("FORCESTOP");
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
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			find = false;
		}
		if (find == false) {
			if(PhoneType.getInstance().m_asForceStopList != null){
				PhoneType.getInstance().m_asForceStopList.clear();
			}
		}
		return find;
	}
}
