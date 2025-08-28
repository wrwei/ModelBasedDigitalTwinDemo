var KEYCODE_TO_CODE = {
  '38': 'ArrowUp',
  '37': 'ArrowLeft',
  '40': 'ArrowDown',
  '39': 'ArrowRight',
  '87': 'KeyW',
  '65': 'KeyA',
  '83': 'KeyS',
  '68': 'KeyD'
};
// var THREE = require('../lib/three');

// from aframe/src/utils/index.js
var shouldCaptureKeyEvent = function (event) {
  if (event.metaKey) { return false; }
  return document.activeElement === document.body;
};

var CLAMP_VELOCITY = 0.00001;
var MAX_DELTA = 0.5;
var KEYS = [
  'KeyW', 'KeyA', 'KeyS', 'KeyD', 'KeyT', 'KeyG',
  'KeyQ', 'KeyE', 'KeyR', 'KeyF',
  'ArrowUp', 'ArrowLeft', 'ArrowRight', 'ArrowDown'
];

/**
 * WASDQERF component to control entities using WASDQERF keys.
 * highly based of WASD componenent part of startard a frame
 */
AFRAME.registerComponent('wasdqerf-controls', {
  schema: {
    acceleration: {default: 65},
    adAxis: {default: 'x', oneOf: ['x', 'y', 'z']},
    adEnabled: {default: true},
    adInverted: {default: false},
    enabled: {default: true},
    fly: {default: false},
    wsAxis: {default: 'z', oneOf: ['x', 'y', 'z']},
    wsEnabled: {default: true},
    wsInverted: {default: false},
    tgAxis: {default: 'y', oneOf: ['x', 'y', 'z']},
    tgEnabled: {default: true},
    tgInverted: {default: true},

    rotationalAcceleration: {default: 10},
    qeAxis: {default: 'y', oneOf: ['x', 'y', 'z']},
    qeEnabled: {default: true},
    qeInverted: {default: true},
    rfAxis: {default: 'x', oneOf: ['x', 'y', 'z']},
    rfEnabled: {default: true},
    rfInverted: {default: true},

  },

  init: function () {
    // To keep track of the pressed keys.
    this.keys = {};
    this.easing = 1.1;

    this.velocity = new THREE.Vector3();
    this.rotationalVelocity = new THREE.Euler(0, 0, 0, 'XYZ');
    this.helperDirectionX = new THREE.Vector3(0, 0, 0);
    this.helperDirectionY = new THREE.Vector3(0, 0, 0);
    this.helperDirectionZ = new THREE.Vector3(0, 0, 0);
    this.helperChangeX = new THREE.Quaternion(0, 0, 0, 0);
    this.helperChangeY = new THREE.Quaternion(0, 0, 0, 0);
    this.helperChangeZ = new THREE.Quaternion(0, 0, 0, 0);
    this.right = new THREE.Vector3(1, 0, 0);
    this.up = new THREE.Vector3(0, 1, 0);
    this.forward = new THREE.Vector3(0, 0, 1);

    // Bind methods and add event listeners.
    this.onBlur = this.onBlur.bind(this);
    this.onContextMenu = this.onContextMenu.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onKeyUp = this.onKeyUp.bind(this);
    this.onVisibilityChange = this.onVisibilityChange.bind(this);
    this.attachVisibilityEventListeners();
  },

  tick: function (time, delta) {
    var data = this.data;
    var el = this.el;
    var velocity = this.velocity;
    var rotationalVelocity = this.rotationalVelocity;

    if (!velocity[data.adAxis] && !velocity[data.wsAxis] && !velocity[data.tgAxis] && !rotationalVelocity[data.qeAxis] && !rotationalVelocity[data.rfAxis] &&
        isEmptyObject(this.keys)) { return; }

    // Update velocity.
    delta = delta / 1000;
    this.updateVelocity(delta);

    if (!velocity[data.adAxis] && !velocity[data.wsAxis] && !velocity[data.tgAxis] && !rotationalVelocity[data.qeAxis] && !rotationalVelocity[data.rfAxis]) { return; }

    // Get movement vector and translate position.
    el.object3D.position.add(this.getMovementVector(delta));

    el.object3D.rotation.setFromQuaternion(this.getRotatationQuaternion(delta));
  },

  update: function (oldData) {
    // Reset velocity if axis have changed.
    if (oldData.adAxis !== this.data.adAxis) { this.velocity[oldData.adAxis] = 0; }
    if (oldData.wsAxis !== this.data.wsAxis) { this.velocity[oldData.wsAxis] = 0; }
    if (oldData.tgAxis !== this.data.tgAxis) { this.velocity[oldData.tgAxis] = 0; }
    if (oldData.qeAxis !== this.data.qeAxis) { this.rotationalVelocity[oldData.qeAxis] = 0; }
    if (oldData.rfAxis !== this.data.rfAxis) { this.rotationalVelocity[oldData.rfAxis] = 0; }
  },

  remove: function () {
    this.removeKeyEventListeners();
    this.removeVisibilityEventListeners();
  },

  play: function () {
    this.attachKeyEventListeners();
  },

  pause: function () {
    this.keys = {};
    this.removeKeyEventListeners();
  },

  updateVelocity: function (delta) {
    var acceleration;
    var adAxis;
    var adSign;
    var data = this.data;
    var keys = this.keys;
    var velocity = this.velocity;
    var wsAxis;
    var wsSign;
    var tgAxis;
    var tgSign;
    var rotationalVelocity = this.rotationalVelocity;
    var rotationalAcceleration;
    var qeAxis;
    var qeSign;
    var rfAxis;
    var rfSign;

    adAxis = data.adAxis;
    wsAxis = data.wsAxis;
    tgAxis = data.tgAxis;
    qeAxis = data.qeAxis;
    rfAxis = data.rfAxis;

    // If FPS too low, reset velocity.
    if (delta > MAX_DELTA) {
      velocity[adAxis] = 0;
      velocity[wsAxis] = 0;
      velocity[tgAxis] = 0;
      rotationalVelocity[qeAxis] = 0;
      rotationalVelocity[rfAxis] = 0;
      return;
    }

    // https://gamedev.stackexchange.com/questions/151383/frame-rate-independant-movement-with-acceleration
    var scaledEasing = Math.pow(1 / this.easing, delta * 60);
    // Velocity Easing.
    if (velocity[adAxis] !== 0) {
      velocity[adAxis] = velocity[adAxis] * scaledEasing;
    }
    if (velocity[wsAxis] !== 0) {
      velocity[wsAxis] = velocity[wsAxis] * scaledEasing;
    }
    if (velocity[tgAxis] !== 0) {
      velocity[tgAxis] = velocity[tgAxis] * scaledEasing;
    }
    if (rotationalVelocity[qeAxis] !== 0) {
        rotationalVelocity[qeAxis] = rotationalVelocity[qeAxis] * scaledEasing;
    }
    if (rotationalVelocity[rfAxis] !== 0) {
        rotationalVelocity[rfAxis] = rotationalVelocity[rfAxis] * scaledEasing;
    }

    // Clamp velocity easing.
    if (Math.abs(velocity[adAxis]) < CLAMP_VELOCITY) { velocity[adAxis] = 0; }
    if (Math.abs(velocity[wsAxis]) < CLAMP_VELOCITY) { velocity[wsAxis] = 0; }
    if (Math.abs(velocity[tgAxis]) < CLAMP_VELOCITY) { velocity[tgAxis] = 0; }
    if (Math.abs(rotationalVelocity[qeAxis]) < CLAMP_VELOCITY) { rotationalVelocity[qeAxis] = 0; }
    if (Math.abs(rotationalVelocity[rfAxis]) < CLAMP_VELOCITY) { rotationalVelocity[rfAxis] = 0; }

    if (!data.enabled) { return; }

    // Update velocity using keys pressed.
    acceleration = data.acceleration;
    if (data.adEnabled) {
      adSign = data.adInverted ? -1 : 1;
      if (keys.KeyA || keys.ArrowLeft) { velocity[adAxis] -= adSign * acceleration * delta; }
      if (keys.KeyD || keys.ArrowRight) { velocity[adAxis] += adSign * acceleration * delta; }
    }
    if (data.wsEnabled) {
      wsSign = data.wsInverted ? -1 : 1;
      if (keys.KeyW || keys.ArrowUp) { velocity[wsAxis] -= wsSign * acceleration * delta; }
      if (keys.KeyS || keys.ArrowDown) { velocity[wsAxis] += wsSign * acceleration * delta; }
    }
    if (data.tgEnabled) {
      tgSign = data.tgInverted ? -1 : 1;
      if (keys.KeyT) { velocity[tgAxis] -= tgSign * acceleration * delta; }
      if (keys.KeyG) { velocity[tgAxis] += tgSign * acceleration * delta; }
    }

    rotationalAcceleration = data.rotationalAcceleration;
    if (data.qeEnabled) {
      qeSign = data.qeInverted ? -1 : 1;
      if (keys.KeyQ) { rotationalVelocity[qeAxis] -= qeSign * rotationalAcceleration * delta; }
      if (keys.KeyE) { rotationalVelocity[qeAxis] += qeSign * rotationalAcceleration * delta; }
    }
    if (data.rfEnabled) {
      rfSign = data.rfInverted ? -1 : 1;
      if (keys.KeyR) { rotationalVelocity[rfAxis] -= rfSign * rotationalAcceleration * delta; }
      if (keys.KeyF) { rotationalVelocity[rfAxis] += rfSign * rotationalAcceleration * delta; }
    }
  },

  getMovementVector: (function () {
    var directionVector = new THREE.Vector3(0, 0, 0);
    var rotationEuler = new THREE.Euler(0, 0, 0, 'YXZ');

    return function (delta) {
      var rotation = this.el.getAttribute('rotation');
      var velocity = this.velocity;
      var xRotation;

      directionVector.copy(velocity);
      directionVector.multiplyScalar(delta);

      // Absolute.
      if (!rotation) { return directionVector; }

      xRotation = this.data.fly ? rotation.x : 0;

      // Transform direction relative to heading.
      rotationEuler.set(THREE.MathUtils.degToRad(xRotation), THREE.MathUtils.degToRad(rotation.y), 0);
      directionVector.applyEuler(rotationEuler);
      return directionVector;
    };
  })(),

  getRotatationQuaternion: (function () {
    var resultQuartnernion = new THREE.Quaternion();

    return function (delta) {
      let rotation = this.el.object3D.rotation;
      let rotationalVelocity = this.rotationalVelocity;

      let helperDirectionX = this.helperDirectionX;
      let helperDirectionY = this.helperDirectionY;
      let helperDirectionZ = this.helperDirectionZ;

      let helperChangeX = this.helperChangeX;
      let helperChangeY = this.helperChangeY;
      let helperChangeZ = this.helperChangeZ;

      resultQuartnernion.setFromEuler(rotation);

      let up = helperDirectionY.copy(this.up);//.applyQuaternion(resultQuartnernion);
      resultQuartnernion.premultiply(helperChangeY.setFromAxisAngle(up, rotationalVelocity.y * delta));

      let forward = helperDirectionZ.copy(this.forward).applyQuaternion(resultQuartnernion);
      resultQuartnernion.premultiply(helperChangeZ.setFromAxisAngle(forward, rotationalVelocity.z * delta));

      let right = helperDirectionX.copy(this.right).applyQuaternion(resultQuartnernion);
      resultQuartnernion.premultiply(helperChangeX.setFromAxisAngle(right, rotationalVelocity.x * delta));

      return resultQuartnernion;
    };
  })(),

  attachVisibilityEventListeners: function () {
    window.oncontextmenu = this.onContextMenu;
    window.addEventListener('blur', this.onBlur);
    window.addEventListener('focus', this.onFocus);
    document.addEventListener('visibilitychange', this.onVisibilityChange);
  },

  removeVisibilityEventListeners: function () {
    window.removeEventListener('blur', this.onBlur);
    window.removeEventListener('focus', this.onFocus);
    document.removeEventListener('visibilitychange', this.onVisibilityChange);
  },

  attachKeyEventListeners: function () {
    window.addEventListener('keydown', this.onKeyDown);
    window.addEventListener('keyup', this.onKeyUp);
  },

  removeKeyEventListeners: function () {
    window.removeEventListener('keydown', this.onKeyDown);
    window.removeEventListener('keyup', this.onKeyUp);
  },

  onContextMenu: function () {
    var keys = Object.keys(this.keys);
    for (var i = 0; i < keys.length; i++) {
      delete this.keys[keys[i]];
    }
  },

  onBlur: function () {
    this.pause();
  },

  onFocus: function () {
    this.play();
  },

  onVisibilityChange: function () {
    if (document.hidden) {
      this.onBlur();
    } else {
      this.onFocus();
    }
  },

  onKeyDown: function (event) {
    var code;
    if (!shouldCaptureKeyEvent(event)) { return; }
    code = event.code || KEYCODE_TO_CODE[event.keyCode];
    if (KEYS.indexOf(code) !== -1) { this.keys[code] = true; }
  },

  onKeyUp: function (event) {
    var code;
    code = event.code || KEYCODE_TO_CODE[event.keyCode];
    delete this.keys[code];
  }
});

function isEmptyObject (keys) {
  var key;
  for (key in keys) { return false; }
  return true;
}
