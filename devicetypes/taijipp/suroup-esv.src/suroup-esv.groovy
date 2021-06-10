/**
 *  suroup-esv
 *
 *  Copyright 2021 taijipp@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "suroup-esv", namespace: "taijipp", author: "taijipp@gmail.com", vid: "generic-switch", ocfDeviceType: "oic.d.fan") {
		capability "Fan Speed"
		capability "Switch"
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
        
        attribute "fanLevel", "enum", ["0", "1", "2", "3"]    
        
        command "fanLevel", ["string"]
	}


	simulator { }
	preferences { }
}

def installed() {
	log.debug "installed()"
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle attributes
}

def setStatus(params){
    log.debug "${params.key} : ${params.data}"
    
    switch(params.key){
    case "fanLevel":
    	sendEvent(name:"fanLevel", value: params.data)
    	break;
    }
}
// handle commands

def off() {
	log.debug "Executing 'off'"
	setProperty("fanSpeed", "0")
    //setFanSpeed(0)
}

def on() {
	setProperty("fanSpeed", "1")
    //setFanSpeed(1)
}

def setFanSpeed(speed){
	log.debug "Fan Speed '${speed}'"
	setProperty("fanSpeed", "${speed}")
}

def fanLevel(speed) {
	log.debug "Fan LEVEL '${speed}'"
	setProperty("fanSpeed", "${speed}")
}

//def setBypass(bypass){
//	log.debug "bypass '${bypass}'"
//	setProperty("bypass", "${bypass}")
//}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
	sendHubCommand(myhubAction)
}

def refresh() {
	log.debug "Executing 'refresh'"
	try{
		def options = [
			"method": "GET",
			"path": state.path,
			"headers": [
				"HOST": state.address,
				"Content-Type": "application/json"
			]
		]
		sendCommand(options, refreshCallback)
	} catch(e) {
		log.error "Refresh Error!!! ${e}"
	}
}

def refreshCallback(physicalgraph.device.HubResponse hubResponse) {
	def msg
	try {
		msg = parseLanMessage(hubResponse.description)
		log.debug msg.json

		updateProperty("switch", msg.json.property.switch)
		updateProperty("fanSpeed", msg.json.property.fanSpeed)
		//updateProperty("bypass", msg.json.property.bypass)
	} catch (e) {
		log.error("Exception caught while parsing data: "+e)
	}
}

def setUrl(String url){
	log.debug "URL >> ${url}"
	state.address = url
}

def setPath(String path){
	log.debug "path >> ${path}"
	state.path = path
}

def updateProperty(propertyName, propertyValue) {
	log.debug "updateProperty >> ${propertyName}, ${propertyValue}"
	switch(propertyName) {
		case "switch":
		//case "bypass":
			sendEvent(name:propertyName, value:propertyValue)
			break
		case "fanSpeed":
			sendEvent(name:propertyName, value:propertyValue)
			if (propertyValue == "0") {
				sendEvent(name:"switch", value:"off")
			} else {
				sendEvent(name:"switch", value:"on")
			}
			//if (propertyValue == "4") {
			//	sendEvent(name: "fanSpeed", value: 3)
			//} else {
			//	sendEvent(name: "fanSpeed", value: propertyValue)
            //}
			break
		default:
			log.debug "UNKNOWN PROPERTY!! ${propertyName}"
	}
}

def setProperty(String name, String value) {
	try{    
	log.debug "setProperty >> ${name}, ${value}"
		def options = [
			"method": "PUT",
			"path": state.path + "/" + name + "/" + value,
			"headers": [
				"HOST": state.address,
				"Content-Type": "application/json"
			]
		]
		sendCommand(options, setPropertyCallback)
	} catch(e) {
		log.error "Error!!! ${e}"
	}
}

def setPropertyCallback(physicalgraph.device.HubResponse hubResponse) {
	def msg
	try {
		msg = parseLanMessage(hubResponse.description)
		log.debug msg.json
	} catch (e) {
		log.error("Exception caught while parsing data: "+e);
	}
}