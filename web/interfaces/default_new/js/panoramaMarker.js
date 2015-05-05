function BaseMarker() {
  this._scrX = 0;
  this._scrY = 0;
  this._live = true;
}

BaseMarker.prototype = {
  constructor: BaseMarker,
  setPosition: function(x, y) {
    this._scrX = x;
    this._scrY = y;
    this.style.left = x + 'px';
    this.style.top = y + 'px';
  },
  getX: function() { return this._scrX; },
  getY: function() { return this._scrY; },
  visible: function(value) {
    var isVisible = (this.style.display != 'none');
     if(value === undefined) {
      return isVisible;
    } else {
      if(value != isVisible) {
        this.style.display = (value)?'block':'none';
      }
      return value;
    }
  },
  activate: function() {
    this._live = true;
  },
  deactivate: function() {
    this._live = false;
  }
};

function NavMarker(_hash, initObj) {
  BaseMarker.call(this);
  this.className = 'navigate-point';
  this.hash = _hash;
  this.position = new THREE.Vector3(initObj.x, initObj.y, initObj.z);
  this.unitVector = new THREE.Vector3(initObj.x,  initObj.y, initObj.z).normalize();
}

NavMarker.prototype = new BaseMarker();
NavMarker.prototype.constructor = NavMarker;

//factory function to create a PosMarker extends a Div element
NavMarker.create = function(index, initObj) {
  var element = document.createElement('div');
  $.extend(element, NavMarker.prototype);
  NavMarker.apply(element, arguments);
  return element;
};
