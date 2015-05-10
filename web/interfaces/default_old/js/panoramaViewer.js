// Array element swap code by Richard Scarrott
// http://jsperf.com/array-prototype-move
Array.prototype.move = function(pos1, pos2) {
    // local variables
    var i, tmp;
    // cast input parameters to integers
    pos1 = parseInt(pos1, 10);
    pos2 = parseInt(pos2, 10);
    // if positions are different and inside array
    if (pos1 !== pos2 && 0 <= pos1 && pos1 <= this.length && 0 <= pos2 && pos2 <= this.length) {
	// save element from position 1
	tmp = this[pos1];
	// move element down and shift other elements up
	if (pos1 < pos2) {
	    for (i = pos1; i < pos2; i++) {
		this[i] = this[i + 1];
	    }
	}
	// move element up and shift other elements down
	else {
	    for (i = pos1; i > pos2; i--) {
		this[i] = this[i - 1];
	    }
	}
	// put element from position 1 to destination
	this[pos2] = tmp;
    }
}

var panoContainer, camera, scene, renderer, projector;

var mouse = { x: 0, y: 0 };

var fov = 70, maxFov = 90, minFov = 15,
    width,height, aspect,
    isUserInteracting = false,
    onMouseDownMouseX = 0, onMouseDownMouseY = 0,
    lon = 0, onMouseDownLon = 0,
    lat = 0, onMouseDownLat = 0,
    phi = 0, theta = 0,
    mesh, sphereRadius = 500,
    meshRotation =  Math.PI / 2 * 3,
    panoSelectionRadius = 40;

var panoDocList = new Array();
panoDocList.ids = new Array();
panoDocList.getDocByIndex = function(index) {
    return panoDocList[panoDocList.ids[index]];
};

var nearbyPanoList = new Array();
nearbyPanoList.ids = new Array();

function initPanoramaViewer() {    
    //Creating the document list to store data about the panoramams
    var jsonNodeDiv = $("#jsonPanoNodes");
    if(jsonNodeDiv.length) {
	var jsonNodehtml = jsonNodeDiv.html();
	var jsonNodes = eval(jsonNodehtml);
	if(jsonNodes && jsonNodes.length > 0) {
	    for(var i = 0; i < jsonNodes.length; i++) {
		panoDocList[jsonNodes[i].nodeID] = jsonNodes[i];
		panoDocList.ids.push(jsonNodes[i].nodeID);
	    }
	} else {
	    $("pano-container").css({visibility:"hidden", height:"0px"});
	}
    }    

    panoContainer = document.getElementById( 'pano-container' );
    // Creating the camera
    // Setting width and height variables as the container dimensions changes when fov is changed
    if(panoContainer.className == "pano_canvas_fullscreen") {
	width = window.innerWidth;
	height = window.innerHeight;
	aspect = width / height;
    } else if(panoContainer.className == "pano_canvas_half") {
	width = panoContainer.offsetWidth;
	height = panoContainer.offsetHeight;
	aspect = width / height;
    }
    camera = new THREE.PerspectiveCamera( fov, aspect, 1, 1100 );
    camera.target = new THREE.Vector3(0, 0, 0 );
    
    // Creating the scene
    scene = new THREE.Scene();
    
    projector = new THREE.Projector();
    
    var sourceFile = gs.documentMetadata[panoDocList.ids[0]].Image;
    var assocfilepath = gs.documentMetadata[panoDocList.ids[0]].assocfilepath;
    var httpPath = gs.collectionMetadata.httpPath;

    var fullPanoURL = httpPath + "/index/assoc/" + assocfilepath + "/" + sourceFile;

    // Creating a sphere with the panorama applied as a texture
    if (Detector.webgl) {
	mesh = new THREE.Mesh( new THREE.SphereGeometry( sphereRadius, 60, 40 ), new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( fullPanoURL ), wireframe: false, overdraw: true, opacity:0 } ) );
    } else {
	mesh = new THREE.Mesh( new THREE.SphereGeometry( sphereRadius, 30, 20 ), new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( fullPanoURL ), wireframe: false, overdraw: true, opacity:0 } ) );
    }
    
    mesh.scale.x = -1;
    // Adding the sphere to the scene
    scene.add( mesh );

    switchPanorama(panoDocList.ids[0]);

    renderer = Detector.webgl? new THREE.WebGLRenderer(): new THREE.CanvasRenderer();
    renderer.setSize(width,height);

    panoContainer.appendChild( renderer.domElement );
    
    // Adding in Mouse events
    panoContainer.addEventListener( 'mousedown', onDocumentMouseDown, false );
    document.addEventListener( 'mousemove', onDocumentMouseMove, false );
    document.addEventListener( 'mouseup', onDocumentMouseUp, false );
    panoContainer.addEventListener( 'mousewheel', onDocumentMouseWheel, false );
    panoContainer.addEventListener( 'DOMMouseScroll', onDocumentMouseWheel, false);

    if(panoContainer.className == "pano_canvas_fullscreen")
	window.addEventListener( 'resize', onWindowResize, false );
}

function degreesToCoords(degrees, radius) {
    return new THREE.Vector3(Math.cos(degrees * Math.PI/180) * radius, 0, Math.sin(degrees * Math.PI/180) * radius);
}


function calculateBearing(from, to) {    
    // x is lat and y is long
    var R = 6371; // km
    var lat1 = from.x * Math.PI / 180;
    var lat2 = to.x * Math.PI / 180;
    var dLon = (to.y-from.y) * Math.PI / 180;
    var y = Math.sin(dLon) * Math.cos(lat2);
    var x = Math.cos(lat1)*Math.sin(lat2) -
	    Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
    var brng = (Math.atan2(y, x)) * 180 / Math.PI;
    return (brng + 360) % 360;
}

function calculateDistance(from, to) {
    // x is lat and y is long
    var R = 6371; // km
    var dLat = (from.x-to.x) * Math.PI / 180;
    var dLon = (from.y-to.y) * Math.PI / 180;
    var lat1 = from.x * Math.PI / 180;
    var lat2 = to.x * Math.PI / 180;

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    var d = R * c;
    // return distance in metres
    return d * 1000;
}

function switchPanorama( panoramaID, destMarker ) {
    // Building the file path
    var sourceFile = gs.documentMetadata[panoramaID].Image;
    var assocfilepath = gs.documentMetadata[panoramaID].assocfilepath;
    var httpPath = gs.collectionMetadata.httpPath;

    var fullPanoURL = httpPath + "/index/assoc/" + assocfilepath + "/" + sourceFile;

    // Creating the material
    if (Detector.webgl) {
	var texture =  new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( fullPanoURL ), opacity: 0 } );
    } else {
	var texture =  new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( fullPanoURL ), opacity: 0, wireframe: false, overdraw: true } );
    }
    
    if (destMarker) {
	new TWEEN.Tween(mesh.position).to({x: -destMarker.position.x, y: -destMarker.position.y, z: -destMarker.position.z}, 500).onComplete(function () {
		mesh.position = new THREE.Vector3(destMarker.position.x,destMarker.position.y,destMarker.position.z);
		new TWEEN.Tween(mesh.position).to({x: 0, y: 0,z: 0},500).start();
	}).start();	
    }
	
    new TWEEN.Tween(mesh.materials[0]).to({opacity: 0}, 500).onComplete(function () {
	    // Rotation
	    if (gs.documentMetadata[panoramaID].Angle) {
		var temp = 360 + parseFloat(gs.documentMetadata[panoramaID].Angle);
		temp = temp % 360;
		console.log(temp);
		mesh.rotation.y =  meshRotation + (gs.documentMetadata[panoramaID].Angle * Math.PI / 180);
	    } else if (gs.documentMetadata[panoramaID].cv_rotation) 
		mesh.rotation.y =  meshRotation + (gs.documentMetadata[panoramaID].cv_rotation * Math.PI / 180);
	    else
		mesh.rotation.y = meshRotation;
	    
	    mesh.materials[0] = texture;		
	    new TWEEN.Tween(mesh.materials[0]).to({opacity: 1}, 500).start();
    }).start();
    
    
    // Checking if there are markers to remove
    if (nearbyPanoList.length > 0) {
	for(var i = 0; i < nearbyPanoList.length; i++) {
	    panoContainer.removeChild(nearbyPanoList[i]);
	    // document.body.appendChild(nearbyPanoList[i]);
	    nearbyPanoList[i].deactivate();
	}
    }

    // Clearing the array
    nearbyPanoList = new Array();

    // Moving the selected pano to the front of the panolist array
    panoDocList.move(panoDocList.ids.indexOf(panoramaID), 0);
    panoDocList.ids.move(panoDocList.ids.indexOf(panoramaID), 0);

    var startPanoLonLat = new THREE.Vector3(gs.documentMetadata[panoramaID].Latitude,gs.documentMetadata[panoramaID].Longitude);

    // going through the panolist checking the distance
    for(var i = 1; i < panoDocList.ids.length; i++) {
	var endPanoLonLat = new THREE.Vector3(gs.documentMetadata[panoDocList.ids[i]].Latitude,gs.documentMetadata[panoDocList.ids[i]].Longitude);
	if(calculateDistance(startPanoLonLat,endPanoLonLat) < panoSelectionRadius) {
	    var bearing = calculateBearing(startPanoLonLat,endPanoLonLat); 
	    var pos =  degreesToCoords(bearing, sphereRadius);
	    var navMarker = NavMarker.create(panoDocList.ids[i],pos);	
	    nearbyPanoList.push(navMarker);
	    //navMarker.addEventListener('click', switchPanorama(hash), false);
	    navMarker.addEventListener('click', function(e) {
		    var sender = (e && e.target) || (window.event && window.event.srcElement);
		    switchPanorama(sender.hash, sender); }, false);
	}
    }
    
    // Going through the new nearbyPanoList and adding in the markers to the scene
    for(var i = 0; i < nearbyPanoList.length; i++) {
	panoContainer.appendChild(nearbyPanoList[i]);
	nearbyPanoList[i].activate();
    }
    
} 


function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();

    renderer.setSize( window.innerWidth, window.innerHeight );
}


function onDocumentMouseDown( event ) {
    event.preventDefault();
    isUserInteracting = true;
      onPointerDownPointerX = event.clientX;
      onPointerDownPointerY = event.clientY;
      onPointerDownLon = lon;
      onPointerDownLat = lat;
      
}

function onDocumentMouseMove( event ) {
    if ( isUserInteracting ) {
	lon = ( onPointerDownPointerX - event.clientX ) * 0.1 + onPointerDownLon;
	lat = ( event.clientY - onPointerDownPointerY ) * 0.1 + onPointerDownLat;
    }
}

function onDocumentMouseUp( event ) {
    var vector = new THREE.Vector3( mouse.x, mouse.y, 0.5 );
    projector.unprojectVector( vector, camera );
    isUserInteracting = false;
}

function onDocumentMouseWheel( event ) {
    if ( event.wheelDeltaY ) {
	// WebKit
	fov -= event.wheelDeltaY * 0.05;
    } else if ( event.wheelDelta ) {
	// Opera / Explorer 9	
	fov -= event.wheelDelta * 0.05;
    } else if ( event.detail ) {
	// Firefox	
	fov += event.detail * 1.0;
    }

    if ( fov > maxFov) {
	fov = maxFov;
    } else if (fov < minFov) {
	fov = minFov;
    }

    camera.projectionMatrix = THREE.Matrix4.makePerspective( fov, aspect , 1, 1100 );
    _render();
}

function _animate() {
    requestAnimationFrame( _animate );
    _render();
    TWEEN.update();
    
}

function _render() {
    lat = Math.max( - 85, Math.min( 85, lat ) );
    phi = ( 90 - lat ) * Math.PI / 180;
    theta = lon * Math.PI / 180;
 
    camera.target.x = sphereRadius * Math.sin( phi ) * Math.cos( theta );
    camera.target.y = sphereRadius * Math.cos( phi );
    camera.target.z = sphereRadius * Math.sin( phi ) * Math.sin( theta );
    
    camera.lookAt( camera.target );
    
    /*
    // distortion
    camera.position.x = - camera.target.x;
    camera.position.y = - camera.target.y;
    camera.position.z = - camera.target.z;
    */

    var camUnitVector = camera.target.clone().normalize();
    var i, angle, sameSide, p2D, marker;

    // Snippet of code from Thanh Tran from in2ideas
    // thanh.tran@in2ideas.com
    for (i = 0; i < nearbyPanoList.length; ++i) {
	marker =  nearbyPanoList[i];
	p2D = projector.projectVector(marker.position.clone(), camera);
	
	p2D.x = (p2D.x + 1)/2 * width;
	p2D.y = - (p2D.y - 1)/2 * height;
	
	angle = Math.acos(camUnitVector.dot(marker.unitVector)) * 180 / 3.14;
	sameSide = (angle < 90);
	
	if(!sameSide || p2D.x < 0 || p2D.x > width ||
	   p2D.y < 0 || p2D.y > height) {
	    marker.visible(false);
	} else {
	    marker.visible(true);
	    marker.setPosition(p2D.x, p2D.y);
	}
    }
    renderer.render( scene, camera );
}