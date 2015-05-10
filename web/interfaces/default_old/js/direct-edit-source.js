(function() {
	// DEBUG DEFINITION
	/*
	 * file: Debug.js
	 *
	 * @BEGINLICENSE
	 * Copyright 2010 Brook Novak  (email : brooknovak@seaweed-editor.com) 
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License as published by
	 * the Free Software Foundation; either version 2 of the License, or
	 * (at your option) any later version.
	 * This program is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 * GNU General Public License for more details.
	 * You should have received a copy of the GNU General Public License
	 * along with this program; if not, write to the Free Software
	 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
	 * @ENDLICENSE
	 */
	/**
	 * @namespace The debug namespace will be removed in release builds.
	 */
	debug = {};

	/**
	 * Asserts a condition. If a condition fails a alert box shows and an exception is thrown.
	 * 
	 * @param {Boolean} cond A condition
	 * 
	 * @param {String} msg An optional message
	 * 
	 * @throws {Error} if a condition fails
	 */
	debug.assert = function(cond, msg) {

	    try {
	        if (!cond) throw new Error("Assertion failed" + (msg ? ": " + msg : ""));
	    } catch (e) {
			
			var fullMsg = e.message + (e.stack ? "\nstack:\n" + e.stack : "");
			
			alert(fullMsg);

			throw e;
	    }
	}

	debug.close = function(){
	    
	    debug.stop = true;
	    
	    if (typeof _debugConsole != "undefined" && _debugConsole) {
	        
	        _debugConsole.parentNode.removeChild(_debugConsole);
	        
	        _debugConsole = null;
	        
	    }
	}

	debug.stop = false;

	/**
	 * Prints a message to a debug console.
	 * 
	 * @param {String} msg A message to print.
	 */
	debug.print = function(msg){
	    
	    if (debug.stop)
	        return;

	    if (window.console && console.log) {
	        console.log(msg);
	    } else if (window.opera && window.opera.postError){
	        window.opera.postError(msg);
	    } else {
	    
	        // Setup debug console
	        if (typeof _debugConsole == "undefined" || !_debugConsole) {

	            _debugConsole = document.createElement("div");
	            _debugConsole.style.backgroundColor = "#444444";
	            _debugConsole.style.position = "fixed";
	            _debugConsole.style.border = "2px solid #000000";
	            _debugConsole.style.width = "320px";
	            _debugConsole.style.height = "174px";
	            _debugConsole.style.zIndex = "9999";
	            _debugConsole.style.top = "0";
	            _debugConsole.style.left = "0";
	            
	            _debugConsole.innerHTML = '<div style="width:320px; color:white; font-weight:bold; font-family:arial,sans; text-align:center; font-size:14px;"><div style="float:right;border: 1px dashed black; color:black; background-color:#66D390; padding:4px; cursor:pointer;" onclick="debug.close();">close</div>~Seaweed~ Debug Console</div>';
	            
	            _debugOutput = document.createElement("div");
	            _debugOutput.style.backgroundColor = "white";
	            _debugOutput.style.overflow = "scroll";
	            _debugOutput.style.width = _debugConsole.style.width;
	            _debugOutput.style.height = "140px";
	            
	            _debugConsole.appendChild(_debugOutput);
	            
	        }
	        
	        if (document.body && _debugConsole && _debugConsole.parentNode != document.body)
	            document.body.appendChild(_debugConsole);
	                
	        _debugOutput.innerHTML += msg.replace(/\n/g,"<br/>");
	        var st = _debugOutput.scrollHeight - _debugOutput.clientHeight;
	        if (st < 0) st = 0;
	        _debugOutput.scrollTop = st;

	    }
	}

	/**
	 * Prints a message to a debug console with a new line
	 * 
	 * @param {String} msg A message to print.
	 */
	debug.println = function(msg){
	    debug.print(msg + "\n");
	};

	/**
	 * Prints out a TODO message and it's stack location
	 * @param {String} msg An optional todo message
	 */
	debug.todo = function(msg) {
	    try {
	       throw new Error();
	    } catch (e) {
	        var fullMsg = "TODO: " + (msg ? msg : "") + (e.stack ? "\nAt" + e.stack : + "");
	        debug.println(fullMsg);
	    }
	};
	//DEBUG DEFINITION END
	// Bootstrap start
	
	// Bootstrap end
	
	/*
	 * file: Core.js
	 * 
	 * @BEGINLICENSE Copyright 2010 Brook Novak (email :
	 * brooknovak@seaweed-editor.com) This program is free software; you can
	 * redistribute it and/or modify it under the terms of the GNU General
	 * Public License as published by the Free Software Foundation; either
	 * version 2 of the License, or (at your option) any later version. This
	 * program is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
	 * more details. You should have received a copy of the GNU General Public
	 * License along with this program; if not, write to the Free Software
	 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
	 * @ENDLICENSE
	 */

	// Short-hands which can be munged
	var
	// Notes:
	// Not in core notation ($...) or internal notation (_...) since not ready
	// for all scripts until library initialized.
	/**
	 * The document.body reference. Available when library initialized.
	 * 
	 * @type Node
	 */
	docBody,

	/**
	 * @type undefined
	 */
	$undefined;

	/**
	 * @namespace The main namespace for the whole system
	 * @author Brook Jesse Novak
	 */
	de = {

		version : "0.0.1",

		/**
		 * @private Module register
		 */
		m : [],

		/**
		 * @namespace Contains useful collections like listed lists and
		 *            hashsets.
		 * @author Brook Jesse Novak
		 */
		collections : {},

		/**
		 * @namespace The DOM events subsystem.
		 * @author Brook Jesse Novak
		 */
		events : {},

		/**
		 * Adds a callback function to be invoked once the DOM is ready. Allows
		 * multiple registrations of handlers. <br/> NOTE: In the debug release,
		 * the window "onload" events are used instead of "domready" events and
		 * therefore take longer to be raised than expected. This is to ensure
		 * that all scripts are downloaded by the bootstrapper.
		 * 
		 * @type Function
		 * @param {Function}
		 *            handler A call back function to be invoked when the
		 *            document is loaded and direct edit can be initialized.
		 */
		onready : (function() {

			var handlers = []; // zeroed when dom ready occurs

			function onReadyHandler() {

				if (handlers) { // First onload event?

					// Fire event to handlers
					for ( var i in handlers) {
						handlers[i]();
					}

					// Mark that onload event has occured
					handlers = 0;
				}
			}
			;

			// @DEBUG ON
			(function() {
				// @DEBUG OFF

				// @DEBUG ON
				// Use window onload in debug release since some broswers can
				// invoke onready
				// events before the bootstrapper download all scripts in the
				// seaweed api.

				// Register to onload event
				if (window.addEventListener)
					window.addEventListener("load", onReadyHandler, false);
				else if (window.attachEvent)
					window.attachEvent("onload", onReadyHandler);
				else {
					// Save exisiting handlers
					if (window.onload)
						handlers.push(window.onload);
					window.onload = onloadFunc;
				}
				return;

				// @DEBUG OFF

				// For the release use DOMReady event - nice and quick.
				// This code is based from JQuerry 1.3.2 bindReady event (MIT
				// licensed code)

				if (document.addEventListener) { // W3C Compliant
					// Use the handy event callback
					document.addEventListener("DOMContentLoaded", function() {
						document.removeEventListener("DOMContentLoaded",
								arguments.callee, false);
						onReadyHandler();

					}, false);

				} else if (document.attachEvent) { // IE
					// Ensure firing before onload.
					// maybe late but safe also for iframes
					document.attachEvent("onreadystatechange", function() {
						if (document.readyState === "complete") {
							document.detachEvent("onreadystatechange",
									arguments.callee);
							onReadyHandler();
						}
					});

					// If IE and not an iframe continually check to see if the
					// document is ready
					if (document.documentElement.doScroll
							&& window == window.top)
						(function() {
							if (!handlers)
								return;

							try {
								// If IE is used, use the trick by Diego Perini
								// http://javascript.nwbox.com/IEContentLoaded/
								document.documentElement.doScroll("left");
							} catch (error) {
								setTimeout(arguments.callee, 0);
								return;
							}

							// and execute any waiting functions
							onReadyHandler();
						})();

					// Fallback: use window onload. NOTE: In release mode this
					// will be present
					// in this very script so it is safe to use it.
					_addHandler(window, "load", function() {
						onReadyHandler();
						_removeHandler(window, "load", arguments.callee);
					});

				}

				// @DEBUG ON
			})();
			// @DEBUG OFF

			// The function
			return function(handler) {
				// Pending onload?
				if (handlers)
					handlers.push(handler);
				else
					handler(); // Immedite exec since onload occured
			};

		})(),

		/**
		 * Prepares DEdit for usage
		 */
		init : function() {

			// Already initialized?
			if (!de.m)
				return;

			docBody = document.body;

			var modules = de.m;

			// @DEBUG ON

	    	// @DEBUG OFF

			// Initialize modules
			var orderChanged;

			do { // sort dependancies in a bubble sort manner
				// NOTE: This does not detect cyclic dependancies... thus can
				// inifitely loop
				// in such cases.

				// TODO: Faster/elegant way of building dependency tree

				orderChanged = false;
				for (var i = 0; i < modules.length; i++) {

					var currentMod = modules[i];

					// If this has no dependancies then leave as is
					if (!currentMod.depends)
						continue;

					// Check that dependancies occur before this
					for (var j = 0; j < currentMod.depends.length; j++) {

						for (var k = i + 1; k < modules.length; k++) {
							if (currentMod.depends[j] == modules[k].name) {
								// Move this (modules[i]) to after the
								// dependancy (modules[k])
								var old = modules;
								modules = old.slice(0, i).concat(
										old.slice(i + 1, k + 1).concat(
												[ currentMod ].concat(old
														.slice(k + 1))));
								orderChanged = true;
								break;
							}
						}

						if (orderChanged)
							break;
					}

					if (orderChanged)
						break;

				} // Next module
			} while (orderChanged); // Continue bubble sorting the dependancy
									// list

			// Execute initialization code
			for (i in modules) {
				if (modules[i].init)
					modules[i].init();
			}

			// cleanup initialization breadcrumbs
			delete de["m"];
		}
	};

	/*
	 * Declare core internals inline .. this is a special setup since Core is
	 * the first script declared
	 */

	/**
	 * <p>
	 * Enqueue's an intialization function to be invoked during the DEdit API
	 * initialization phase (after DOM is ready).
	 * </p>
	 * <p>
	 * If a module (script) needs to be initialized before usage and depends on
	 * the document DOM state to be ready - or should only be initialized when
	 * the API is explicitely been asked to be initialized, or initialization
	 * code depends on a public API interface then use this function (at most
	 * once per module).
	 * </p>
	 * NOTE: All internals will be loaded upon execution of the initialization
	 * code, therefore there is no need to delcare dependancies (@DEPENDS or via
	 * this method) for usage of internals within initialization code.
	 * 
	 * @param {String}
	 *            moduleName The name of the module (script file name without
	 *            extension).
	 * 
	 * @param {Function}
	 *            init An optional initialization method. If the initialization
	 *            code depends on other modules' public interfaces, then specify
	 *            the module names as additional arguments to this call.
	 */
	function $enqueueInit(moduleName, init) {

		// @DEBUG ON

		debug
				.assert(de.m ? true : false,
						"Attempted to enqueue initializor function after API has initialized");

		for ( var i in de.m) { // Integrity check
			debug.assert(de.m[i].name != moduleName,
					'An initializor is already registered under the name "'
							+ moduleName + '"');
		}

		// @DEBUG OFF

		// Discover dependancies (declared as extra arguments)
		var dependancies = Array.prototype.slice.call(arguments);
		dependancies.splice(0, 2);

		// Store the initializor for the module
		de.m.push({
			name : moduleName,
			init : init,
			depends : dependancies
		});

	}
	;

	/**
	 * Adds/suppliments all members to a target object from a source object. If
	 * the target object has a member that is also contained in source, it will
	 * be overridden with the source member.
	 * 
	 * Leaves the source object in tact.
	 * 
	 * @param {Object}
	 *            target The destination object
	 * 
	 * @param {Object}
	 *            source The source object
	 * 
	 * @param {Boolean}
	 *            override (Optional) True to override existing members on
	 *            conflicts, false skip conflicts. Defaults to true
	 * 
	 * @return {Object} The target object
	 */
	function $extend(target, source, override) {

		if (override !== false)
			override = true;

		for ( var mem in source) {
			if (override || typeof target[mem] == "undefined")
				target[mem] = source[mem];
		}

		return target;
	}

	/**
	 * Create a hash map of booleans from a comma separated set of keys.
	 * 
	 * @param {String}
	 *            str A comma separated set of keys. White spaces are not
	 *            truncated
	 * @return {Object} A lookup map
	 */
	function $createLookupMap(str) {
		var arr = str.split(",");
		var map = {};
		for ( var i in arr) {
			map[arr[i]] = true;
		}
		return map;
	}

	/**
	 * Shorthand for document.createElement
	 * 
	 * @param {String}
	 *            tag The element name
	 * @return {Element} A new element
	 */
	function $createElement(tag) {
		return document.createElement(tag);
	}
	/* Start * file: DoublyLinkedList.js */
	/**
	 * @class
	 * 
	 * A Doubly Linked List. Supports all data types. Because this collection
	 * creates cyclic references, you should explicitely call
	 * de.collections.DoublyLinkedList.clear when finished with a doubly linked
	 * list to avoid memory leaks in browsers which uses reference counting
	 * garbage collections (e.g. IE and FF).
	 * 
	 * @author Brook Novak
	 */
	var _DoublyLinkedList = function() {

		var cls = function() {

			/**
			 * @memberOf de.collections.DoublyLinkedList Read only member that
			 *           is the current length of the linked list.
			 * @type Number
			 */
			this.length = 0;

			/**
			 * @memberOf de.collections.DoublyLinkedList Read only member that
			 *           is the current head node of the linked list.
			 * @type de.collections.DLLNode
			 */
			this.head = null;

			/**
			 * @memberOf de.collections.DoublyLinkedList Read only member that
			 *           is the current tail node of the linked list.
			 * @type de.collections.DLLNode
			 */
			this.tail = null;
		};

		// Define the class body
		cls.prototype = {

			/**
			 * Adds data to the end of the linked list YYY
			 * 
			 * @param {Object}
			 *            data Data to add
			 */
			add : function(data) {

				// create a new item object, place data in
				var node = {
					data : data,
					next : null,
					prev : null
				};

				// special case: no items in the list yet
				if (this.length == 0) {
					this.head = node;
					this.tail = node;
				} else {

					// attach to the tail node
					this.tail.next = node;
					node.prev = this.tail;
					this.tail = node;
				}

				// don't forget to update the count
				this.length++;

			},

			/**
			 * Removes an item from the list.
			 * 
			 * @param {Object}
			 *            item An item to remove.
			 * @return {Boolean} True if item existed and was removed. False if
			 *         the item did not exist.
			 */
			remove : function(item) {
				var current = this.head;
				while (current) {
					if (current.data == item) {
						removeNode(this, current);
						return true;
					}
					current = current.next;
				}
				return false;
			},

			/**
			 * Removes a node at the given index.
			 * 
			 * @param {Number}
			 *            index The index to remove at.
			 * 
			 * @return {Object} The removed data. Otherwise null if index out of
			 *         bounds. Note that if data is null, null will also be
			 *         returned.
			 */
			removeAtIndex : function(index) {

				// check for out-of-bounds values
				if (index > -1 && index < this.length) {

					var current;

					// Choose faster scan direction
					if (index < (this.length / 2)) { // Head to tail
						current = this.head;
						for (var i = 0; i < index; i++) {
							current = current.next;
						}
					} else { // Tail to head
						current = this.tail;
						for (var i = this.length - 1; i > index; i--) {
							current = current.prev;
						}
					}

					removeNode(this, current);

					return current.data;
				}

				return null;
			},

			/**
			 * Removes the last node in the list and returns the item. O(1)
			 * operation
			 * 
			 * @return {Object} The last item in the list.
			 */
			pop : function() {
				return this.removeAtIndex(this.length - 1);
			},

			/**
			 * Removes proceding items after atNode. I.E. atNode becomes the new
			 * tail.
			 * 
			 * @param {Object}
			 *            atNode The which will become the new tail.
			 * 
			 * @return {Boolean} True if the operation succeeded (atNode is a
			 *         node in this linked list), False if the operation failed
			 *         (atNode is not a node in this linked list).
			 */
			chop : function(atNode) {

				var current = this.tail, newLength = this.length;

				// Search for atNode... count how far it is from the tail
				while (current && current != atNode) {
					current = current.prev;
					newLength--;
				}

				// If atNode doesn't exist, return false
				if (!current)
					return false;

				// If atNode has a next-node (i.e. not the tail) then
				// chop off the proceeding nodes
				if (current.next) {

					var removedNodes = current.next;
					while (removedNodes) { // Kill all ref n removed nodes
						removedNodes.prev.next = null;
						removedNodes.prev = null;
						removedNodes = removedNodes.next;
					}

					// Set the new tail
					this.tail = current;

					// Be sure to update the length
					this.length = newLength;
				}

				return true;

			},

			/**
			 * Clears the linked list.. so list becomes empty. Call this
			 * whenever you are finished with a Doubly linked list to avoid
			 * memory leaks caused by cyclic references.
			 */
			clear : function() {
				while (this.head) {
					var node = this.head;
					this.head = node.next;
					node.prev = node.next = null;
				}
				this.tail = null;
				this.length = 0;
			},

			/**
			 * Applies a function to all items in this list from the head to the
			 * tail.
			 * 
			 * @param {Function}
			 *            func A function to apply to each item. Takes one
			 *            argument: the item. Return false to abort iteration.
			 */
			iterate : function(func) {
				var current = this.head;
				while (current) {
					var res = func(current.data);
					if (res === false)
						break;
					current = current.next;
				}
			}

		}; // End clas prototype

		/**
		 * @function
		 * @description An alias for de.collections.DoublyLinkedList#add
		 * 
		 * @see de.collections.DoublyLinkedList#add
		 */
		cls.prototype.push = cls.prototype.add;

		/**
		 * @private Removes a node from a doubly linked list
		 * 
		 * @param {de.collections.DoublyLinkedList}
		 *            dll The DLL to remove from
		 * 
		 * @param {de.collections.DLLNode}
		 *            node The node to remove - must be a node in the DLL.
		 */
		function removeNode(dll, node) {

			if (dll.length == 1) {
				dll.clear();
				return;
			}

			if (node == dll.head) {
				dll.head = node.next;
				dll.head.prev = null;
				node.next = null; // Avoid cyclic reference
			} else if (node == dll.tail) {
				dll.tail = node.prev;
				dll.tail.next = null;
				node.prev = null; // Avoid cyclic reference
			} else {
				node.prev.next = node.next;
				node.next.prev = node.prev;
				node.next = node.prev = null; // Avoid cyclic reference
			}

			dll.length--;
		}

		return cls;
	}();

	/**
	 * Exposure of _DoublyLinkedList internal
	 * 
	 * @see _DoublyLinkedList
	 */
	de.collections.DoublyLinkedList = _DoublyLinkedList;

	/* END OF DoublyLinkedList.js */
	/*
	 * file: MVC.js
	 * 
	 * @BEGINLICENSE Copyright 2010 Brook Novak (email :
	 * brooknovak@seaweed-editor.com) This program is free software; you can
	 * redistribute it and/or modify it under the terms of the GNU General
	 * Public License as published by the Free Software Foundation; either
	 * version 2 of the License, or (at your option) any later version. This
	 * program is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
	 * more details. You should have received a copy of the GNU General Public
	 * License along with this program; if not, write to the Free Software
	 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
	 * @ENDLICENSE
	 */

	/**
	 * Extends the given instance into a model (wrt MVC).
	 * 
	 * @param {Object}
	 *            subject The subject containing model data to be observed.
	 */
	function _model(subject) {

		// The list of registered observers for this subject
		var observers = [], observersToRemove; // Zeroed/undef when not firing
												// event, array when model is
												// firing event.

		$extend(subject, {

			/**
			 * 
			 * Notifies all observers that a specific event occured.
			 * 
			 * @param {String}
			 *            event The event name to fire (excluding the "on"
			 *            prefix). For example, "KeyDown" would invoke
			 *            "onKeyDown" in all observers
			 * 
			 * @param {Object}
			 *            details Optional custom details data
			 */
			fireEvent : function(event) {

				if (observers.length > 0) {

					// Construct additional arguments array
					var i, observer, remObservers, args = Array.prototype.slice
							.call(arguments);

					args.shift();

					// Flag this model as firing
					observersToRemove = [];

					// Fire events on each observer
					for (i in observers) {
						observer = observers[i];

						// If the observer has declared a listener function for
						// this event invoke it
						if (typeof observer.ref["on" + event] == "function")
							observer.ref["on" + event].apply(observer.context,
									args);
					}

					// Reset flag
					remObservers = observersToRemove;
					observersToRemove = 0;

					// If Observers when trying to remove themself during an
					// event fire
					// then safely remove them now.
					for (i in remObservers) {
						this.removeObserver(remObservers[i]);
					}

				}
			},

			/**
			 * Adds an observer for receiving event notifications. If the
			 * observer already exists in the observer set it will not be added
			 * twice.
			 * 
			 * @param {Object}
			 *            observer An observer to add to the set.
			 * 
			 * @param {Object}
			 *            context (Optional) The context at which the events
			 *            should be invoked in. Will default to the observer
			 *            object.
			 * 
			 * @param {Boolean}
			 *            notifiedFirst (Optional) True to be the first observer
			 *            to be notified in the current list. Otherwise it will
			 *            be added to the end of the list
			 */
			addObserver : function(observer, context, notifiedFirst) {

				// Ensure that observer array is a set
				if (observerIndex(observer) != -1)
					return;

				// Create observer instance
				observer = {
					ref : observer,
					context : context || observer
				};

				// Add to list depending on requested order
				if (notifiedFirst)
					observers.unshift(observer);
				else
					observers.push(observer);

			},

			/**
			 * Removes an observer from the subjects observer list.
			 * 
			 * @param {Object}
			 *            observer An observer to remove from the set
			 */
			removeObserver : function(observer) {

				// Avoid removing observers while in a firing-event state since
				// some browsers
				// may miss firing an event on a observer if the observer list
				// is sliced while iterating the list
				if (observersToRemove)
					observersToRemove.push(observer); // Will be removed after
														// firing of events
														// finished

				else {
					var index = observerIndex(observer);
					if (index >= 0)
						observers.splice(index, 1);
				}
			}
		});

		/**
		 * @param {Object}
		 *            observerRef The observer reference to check
		 * 
		 * @return {Number} The index in the observers array at which
		 *         observerRef exists. -1 if not found.
		 */
		function observerIndex(observerRef) {
			for (var i = 0; i < observers.length; i++) {
				if (observers[i].ref == observerRef)
					return i;
			}
			return -1;
		}

	}

	/**
	 * Exposure of model internal
	 * 
	 * @see _model
	 */
	de.model = _model;
	// END MVC.js

	$enqueueInit(
			"Platform",
			function() {

				// Detect text direction locale - relies on DOM being ready for
				// manipulation

				var container = $createElement("p");
				container.style.margin = "0 0 0 0";
				container.style.padding = "0 0 0 0";
				// container.style.textAlign = "start"; // If CSS 3+
				container.style.textAlign = ""; // Explicitly override text
												// align that might be assigned
												// via style sheets

				var span = $createElement("span");
				span.innerHTML = "X";

				container.appendChild(span);
				docBody.appendChild(container);

				// LTR if text position is nearer left of container, RTL if text
				// position is nearer right of container
				_localeDirection = span.offsetLeft < (container.offsetWidth - (span.offsetLeft + span.offsetWidth)) ? "ltr"
						: "rtl";

				debug.println("LOCALE-DIRECTION: " + _localeDirection + "\n");

				// Tidy up
				docBody.removeChild(container);

			});

	/**
	 * The clients operating system. Never null, but can be de.Platform.UNKNOWN.
	 * 
	 * @type Number
	 */
	var _os,

	/**
	 * The clients browser. -1 if unknown.
	 * 
	 * @type Number
	 */
	_browser,

	/**
	 * The browser version as a float. Can be -1 if could not determine the
	 * version.
	 * 
	 * @type Number
	 */
	_browserVersion,

	/**
	 * The clients operating system. -1 if unknown.
	 * 
	 * @type Number
	 */
	_engine,

	/**
	 * The layout/rendering engine. -1 if unavailable.
	 * 
	 * @type Number
	 */
	_engineVersion,

	/**
	 * An enumeration for browser/engine/os types. In the release version these
	 * are replaced with actual values.
	 */
	_Platform = {

		/**
		 * Read Only
		 * 
		 * @final
		 * @type Number
		 */
		UNKNOWN : -1, // @REPLACE _Platform.UNKNOWN -1

		/* BROWSER CONSTANTS */

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		FIREFOX : 1, // @REPLACE _Platform.FIREFOX 1

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		OPERA : 2, // @REPLACE _Platform.OPERA 2

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		IE : 3, // @REPLACE _Platform.IE 3

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		CHROME : 4, // @REPLACE _Platform.CHROME 4

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		SAFARI : 6, // @REPLACE _Platform.SAFARI 6
		// ICAB : 101, // Used as engine contant aswell

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		KONQUEROR : 8, // @REPLACE _Platform.KONQUEROR 8

		/**
		 * Read Only: A browser constant
		 * 
		 * @final
		 * @type Number
		 */
		NETSCAPE : 9, // @REPLACE _Platform.NETSCAPE 9

		// OS CONSTANTS
		/**
		 * Read Only: An OS constant
		 * 
		 * @final
		 * @type Number
		 */
		WINDOWS : 1, // @REPLACE _Platform.WINDOWS 1

		/**
		 * Read Only: An OS constant
		 * 
		 * @final
		 * @type Number
		 */
		MAC : 2, // @REPLACE _Platform.MAC 2

		/**
		 * Read Only: An OS constant
		 * 
		 * @final
		 * @type Number
		 */
		LINUX : 3, // @REPLACE _Platform.LINUX 3

		/* LAYOUT/RENDERING ENGINE CONSTANTS */

		/**
		 * Read Only: A rendering engine constant
		 * 
		 * @final
		 * @type Number
		 */
		GECKO : 1, // @REPLACE _Platform.GECKO 1

		/**
		 * Read Only: A rendering engine constant
		 * 
		 * @final
		 * @type Number
		 */
		TRIDENT : 2, // @REPLACE _Platform.TRIDENT 2

		/**
		 * Read Only: A rendering engine constant
		 * 
		 * @final
		 * @type Number
		 */
		WEBKIT : 3, // @REPLACE _Platform.WEBKIT 3

		/**
		 * Read Only: A rendering engine constant
		 * 
		 * @final
		 * @type Number
		 */
		KHTML : 4, // @REPLACE _Platform.KHTML 4

		/**
		 * Read Only: A rendering engine constant
		 * 
		 * @final
		 * @type Number
		 */
		PRESTO : 5
	// @REPLACE _Platform.PRESTO 5

	};

	// Perform platform detection
	(function() {

		// References:
		// - http://unixpapa.com/js/gecko.html
		// - http://www.quirksmode.org/js/detect.html

		var dataBrowser = [ {
			string : navigator.userAgent,
			subString : "Chrome",
			id : _Platform.CHROME,
			versionSearch : "Chrome"
		}, {
			string : navigator.vendor,
			subString : "Apple",
			id : _Platform.SAFARI,
			versionSearch : "Version"
		}, {
			prop : window.opera,
			id : _Platform.OPERA,
			versionSearch : "Opera"
		}, {
			string : navigator.vendor,
			subString : "KDE",
			id : _Platform.KONQUEROR,
			versionSearch : "Konqueror"
		}, {
			string : navigator.userAgent,
			subString : "Firefox",
			id : _Platform.FIREFOX,
			versionSearch : "Firefox"
		}, {
			// for newer Netscapes (6+)
			string : navigator.userAgent,
			subString : "Netscape",
			id : _Platform.NETSCAPE,
			versionSearch : "Netscape"
		}, {
			string : navigator.userAgent,
			subString : "MSIE",
			id : _Platform.IE,
			versionSearch : "MSIE"
		}, {
			// for older Netscapes (4-)
			string : navigator.userAgent,
			subString : "Mozilla",
			id : _Platform.NETSCAPE,
			versionSearch : "Mozilla"
		} ];

		var dataOS = [ {
			string : navigator.platform,
			subString : "Win",
			id : _Platform.WINDOWS
		}, {
			string : navigator.platform,
			subString : "Mac",
			id : _Platform.MAC
		}, {
			string : navigator.platform,
			subString : "Linux",
			id : _Platform.LINUX
		}, ];

		var dataEngine = [ {
			string : navigator.userAgent,
			subString : "MSIE",
			id : _Platform.TRIDENT,
			versionSearch : "MSIE" // The trident versions are same as browser
									// versions
		}, {
			// It is important that this is above KHTML - since webkit is forked
			// from KHTML (which is still in webkits useragent/nav strings)
			string : navigator.userAgent,
			subString : "WebKit",
			id : _Platform.WEBKIT,
			versionSearch : "WebKit"
		}, {
			// It is important to have this above gecko data. since the user
			// agent
			// can contain gecko
			string : navigator.userAgent,
			subString : "KHTML",
			id : _Platform.KHTML,
			versionSearch : "KHTML"
		}, {
			string : navigator.userAgent,
			subString : "Gecko",
			id : _Platform.GECKO,
			versionSearch : "rv"
		}, {
			prop : window.opera,
			id : _Platform.PRESTO,
			versionSearch : "Presto"
		}, ];

		function findMatchingPlatform(data) {

			for ( var i in data) {

				var dataString = data[i].string, dataProp = data[i].prop;

				if (dataString) {
					if (dataString.indexOf(data[i].subString) != -1)
						return data[i];
				} else if (dataProp)
					return data[i];
			}

			return null;
		}

		function extractVersion(dataString, versionSearchString) {
			if (!versionSearchString)
				return null;
			var index = dataString.indexOf(versionSearchString);
			if (index == -1)
				return null;
			return parseFloat(dataString.substring(index
					+ versionSearchString.length + 1)); // Add one for the "/"
														// between the
														// identifier and the
														// version
		}
		debug.println("Inferring platform...");

		// Get the OS
		_os = findMatchingPlatform(dataOS);

		debug.println("OS: " + (_os ? _os.subString : "UNKNOWN"));

		// Set as the enum...
		_os = _os ? _os.id || _Platform.UNKNOWN : _Platform.UNKNOWN;

		// Get the browser
		_browser = findMatchingPlatform(dataBrowser);

		debug.println("BROWSER: "
				+ (_browser ? _browser.versionSearch : "UNKNOWN"));

		_browserVersion = _Platform.UNKNOWN;

		if (_browser) {

			// Extract the version
			_browserVersion = extractVersion(navigator.userAgent,
					_browser.versionSearch)
					|| extractVersion(navigator.appVersion,
							_browser.versionSearch);

			// Set browser as the enum
			_browser = _browser.id;

		} else
			_browser = _Platform.UNKNOWN;

		debug.println("BROWSER-VERSION: " + _browserVersion);

		// Get the layout engine
		_engine = findMatchingPlatform(dataEngine);
		_engineVersion = _Platform.UNKNOWN;

		debug.println("ENGINE: "
				+ (_engine ? (_engine.subString ? _engine.subString
						: _engine.versionSearch) : "UNKNOWN"));

		if (_engine) {
			_engineVersion = extractVersion(navigator.userAgent,
					_engine.versionSearch);
			_engine = _engine.id;
		} else
			_engine = _Platform.UNKNOWN;

		debug.println("ENGINE-VERSION: " + _engineVersion + "\n");

	})();

	$extend(de, {

		/**
		 * @memberOf de Exposes internal platform enumaration
		 * @see _Platform
		 */
		Platform : _Platform,

		/**
		 * @memberOf de Exposes internal field
		 * @see _os
		 */
		os : _os,

		/**
		 * @memberOf de Exposes internal field
		 * @see _browser
		 */
		browser : _browser,

		/**
		 * @memberOf de Exposes internal field
		 * @see _browserVersion
		 */
		browserVersion : _browserVersion,

		/**
		 * @memberOf de Exposes internal field
		 * @see _engine
		 */
		engine : _engine,

		/**
		 * @memberOf de Exposes internal field
		 * @see _engineVersion
		 */
		engineVersion : _engineVersion,

		/**
		 * @memberOf de The Local text direction. Either "ltr" for Left to right
		 *           or "rtl" for right to left.
		 * @type String
		 */
		localDirection : null
	/* Detected once API initialized */

	});
	// END Platform.js

	var _registerAction = function() {
		alert("hit!!!");
	};

	(function() {

		var actionRepository = {}, history = new _DoublyLinkedList(),

		/*
		 * Always points to the next action to be undoed. Null if there is no
		 * undo history. Note that it can be null if there is redo history...
		 */
		currentActionNode = null,

		/*
		 * Non-zero if executing an action within an action. It represents the
		 * action exec depth, typically it would be 0-1, but sometimes 2.
		 */
		execActionDepth = 0;

		// Setup registor action logic
		_registerAction = function(name, action) {
			debug.assert(!actionRepository[name],
					"Already registered action for " + name);
			actionRepository[name] = action;
		};

		/**
		 * @class
		 * 
		 * The undo manager singleton subject provides facilities for executing,
		 * undoing and redoing de.actions.UndoableAction's. <br>
		 * <br>
		 * Before a action is executed/undon/redone, a "onBeforeAction" event is
		 * fired, where the argument is the action about to be
		 * executed/undon/redone. <br>
		 * <br>
		 * After a action is executed/undon/redone, a "onAfterAction" event is
		 * fired, where the argument is the action that has been
		 * executed/undon/redone.
		 * 
		 * @borrows de.mvc.AbstractSubject#addObserver as this.addObserver
		 * 
		 * @borrows de.mvc.AbstractSubject#removeObserver as this.removeObserver
		 */
		de.UndoMan = {
			/**
			 * Add cap to avoid consuming too much memory... < 0 = unlimited
			 */
			maxHistoryCount : 100,

			ExecFlag : {

				GROUP : 1, // @REPLACE de.UndoMan.ExecFlag.GROUP 1

				UPDATE_SELECTION : 2, // @REPLACE
										// de.UndoMan.ExecFlag.UPDATE_SELECTION
										// 2

				/**
				 * If provided then the undo manager will not store the undoable
				 * operations for undoing/redoing - It will leave the current
				 * operation list in tact after execution and thus the action
				 * will not be undone/redone directly by the undo manager.
				 * 
				 * Used for executing actions within an action, or other
				 * internal specialized situations.
				 */
				DONT_STORE : 4
			// @REPLACE de.UndoMan.ExecFlag.DONT_STORE 4
			},

			/**
			 * Exposure of _registerAction internal.
			 * 
			 * @see _registerAction
			 */
			registerAction : _registerAction,

			/**
			 * 
			 * @param {Number}
			 *            flags (Optional) NOTE: If currently executing in an
			 *            action, then the DONT_STORE flag will be automatically
			 *            set. This allows actions to be combined into one
			 * 
			 * @param {String}
			 *            actionName
			 * 
			 * @return {Object} Action specific result
			 */
			execute : function(flags, actionName) {

				// Setup arguments
				var args = Array.prototype.slice.call(arguments);

				// Set default flags
				if (typeof flags != "number") {
					flags = de.UndoMan.ExecFlag.UPDATE_SELECTION;
					actionName = args[0];
					args.shift();
				} else
					args.splice(0, 2);

				// If already executing in a action
				if (execActionDepth)
					flags = de.UndoMan.ExecFlag.DONT_STORE;

				// @DEBUG ON
				// This can be helpful!!
				if (execActionDepth) {
					debug
							.println("WARNING: executing an action within an action - Undo man setting exec flags to DONT_STORE");
				}
				// @DEBUG OFF

				if ((flags & de.UndoMan.ExecFlag.GROUP) && !currentActionNode)
					_error("Cannot group action to nothing");

				// Check that the action exists
				if (!actionRepository[actionName])
					_error("Unknown action called \"" + actionName + "\"");

				var result, action = actionRepository[actionName], actionData = new _ActionData(
						actionName, flags, de.selection.getRange(false),
						de.selection.getRange(true));

				// Apply action filtering on selection
				if (actionData.selBefore) {
					var eProps = de.doc
							.getEditProperties(actionData.selBefore.startNode);

					if (eProps && eProps.afRE) {
						var reEval = eProps.afRE.test(actionName.toLowerCase()
								+ (actionName == "Format" ? args[0]
										.toLowerCase() : ""));
						if (reEval != eProps.afInclusive)
							return;
					}

				}

				// Notify observers
				this.fireEvent("BeforeExec", actionData);

				// Safety check: there shouldn't be any operations in the
				// current op list if storing them here
				debug.assert((flags & de.UndoMan.ExecFlag.DONT_STORE)
						|| !_getOperations());

				// Execute the operation
				execActionDepth++;
				try {
					result = action.exec.apply(actionData, args);
				} finally {
					execActionDepth--;
				}

				// Add to undo history? I.E: Not returning ops
				if (!(flags & de.UndoMan.ExecFlag.DONT_STORE)) {

					var opList = _getOperations();

					// Did anything occur?
					if (!opList || opList.length == 0) {

						this.fireEvent("AfterExec", actionData);

						// Restore selection to state before action
						if (actionData.selBefore)
							de.selection.setSelection(
									actionData.selBefore.startNode,
									actionData.selBefore.startIndex,
									actionData.selBefore.endNode,
									actionData.selBefore.endIndex, true);

						return result;
					}

					// Destroy any redo-history
					// If the current undo marker is at the very beggining of
					// the
					// list, then reset the list.
					if (!currentActionNode)
						history.clear(); /*
											 * Note: If history is already clear
											 * then this is ok.
											 */
					else if (currentActionNode.next)
						history.chop(currentActionNode);

					// Store the action's operations
					actionData.opList = opList;
					history.push(actionData);

					// Update the undo marker
					currentActionNode = history.tail;

					// Check for max history
					if (history.length > this.maxHistoryCount
							&& this.maxHistoryCount > -1
							&& !(action.flags & de.UndoMan.ExecFlag.GROUP))
						history.removeAtIndex(0);

				}

				// Should update the selection?
				if (flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {

					var selAfter = actionData.selAfter;
					if (!selAfter)
						de.selection.clear();

					else
						de.selection.setSelection(selAfter.startNode,
								selAfter.startIndex, selAfter.endNode,
								selAfter.endIndex, true);

				} else {

					// No need to keep the selection snapshots
					delete actionData["selBefore"];

					if (actionData["selAfter"])
						delete actionData["selAfter"];
				}

				// No need to keep order selection range (only used for action
				// exec benifit)
				delete actionData["selBeforeOrdered"];

				// Notify observers
				this.fireEvent("AfterExec", actionData);

				return result;

			},

			/**
			 * Undos the last action.
			 */
			undo : function() {

				do {

					// If the currentActionNode is already before the head, or
					// there is
					// no history, then return.
					if (!currentActionNode)
						return;

					var actionData = currentActionNode.data;

					// Notify observers
					this.fireEvent("BeforeUndo", actionData);

					try {

						// Undo the operation
						_undoOperations(actionData.opList);

						// Shift the action node along
						currentActionNode = currentActionNode.prev;

					} catch (err) {

						// If the undo failed, the all undo/redo history can
						// become out of
						// sync with the DOM. Therefore lose the history to
						// avoid bugs from
						// snowballing into something worse.
						this.clear();

						throw err;
					}

					// Restore selection
					if (actionData.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
						var selBefore = actionData.selBefore;
						if (!selBefore)
							de.selection.clear();
						else
							de.selection.setSelection(selBefore.startNode,
									selBefore.startIndex, selBefore.endNode,
									selBefore.endIndex, true);
					}

					// Notify observers
					this.fireEvent("AfterUndo", actionData);

				} while (currentActionNode
						&& (actionData.flags & de.UndoMan.ExecFlag.GROUP));

			},

			/**
			 * Redo's the last undo.
			 */
			redo : function() {

				var firstRedo = true;

				while (1) {

					if (history.length == 0
							|| currentActionNode == history.tail)
						return;

					// Is the undo-pointer back to the very start of the
					// history?
					var curAction = currentActionNode ? currentActionNode.next
							: history.head;
					var actionData = curAction.data;

					// Only continue redoing if the action if grouped
					if (!firstRedo
							&& !(actionData.flags & de.UndoMan.ExecFlag.GROUP))
						return;

					currentActionNode = curAction;

					// Notify observers
					this.fireEvent("BeforeRedo", actionData);

					// Re-execute the operations
					try {
						_redoOperations(actionData.opList);
					} catch (err) {

						// If the undo failed, the all undo/redo history can
						// become out of
						// sync with the DOM. Therefore lose the history to
						// avoid bugs from
						// snowballing into something worse.
						this.clear();
						throw err;
					}

					// Set the new selection if it was requested to update the
					// selection
					// with this action
					if (actionData.selAfter) {
						var selAfter = actionData.selAfter;
						if (!selAfter)
							de.selection.clear();
						else
							de.selection.setSelection(selAfter.startNode,
									selAfter.startIndex, selAfter.endNode,
									selAfter.endIndex, true);
					}

					firstRedo = false;

					// Notify observers
					this.fireEvent("AfterRedo", actionData);

				}

			},

			/**
			 * Clears all undo/redo history
			 */
			clear : function() {
				history.clear();
				currentActionNode = null;
			},

			/**
			 * @return {Boolean} True if there is any undo history.
			 */
			hasUndo : function() {
				return currentActionNode != null;
			},

			/**
			 * @return {Boolean} True if there is any redo history.
			 */
			hasRedo : function() {
				return history.length > 0 && currentActionNode != history.tail;
			}

		}; // End undo manager singleton

		// Make undo manager a model
		_model(de.UndoMan);

	})();

	var _ActionData = function() {

		var cls = function(name, flags, selBefore, selBeforeOrdered) {
			this.name = name;
			this.flags = flags;
			this.selBefore = selBefore;
			this.selBeforeOrdered = selBeforeOrdered;
			/* this.opList = null */
		}

		cls.prototype = {

			/**
			 * @return {Node} The top-most editable section changed by this
			 *         action. Undefined if there was none.
			 */
			getEditSection : function() {

				if (this.opList) {
					// Infer from operations list
					for ( var i in this.opList) {
						var op = this.opList[i];
						for ( var mem in op) {
							// Is this a dom node still in the document body?
							if (_isDOMNode(op[mem])
									&& _isAncestor(docBody, op[mem])) {
								var esNode = de.doc
										.getEditSectionContainer(op[mem]);
								if (esNode)
									return esNode;
							}
						}
					}
				}
			}

		};

		return cls;

	}();
	// file: UndoMan.js END
	// Start Util.js
	/**
	 * @function Gets a text node or element in the document at a given pixel
	 *           position.
	 * 
	 * @param Number
	 *            x The X pixel relative to the window.
	 * @param Number
	 *            y The Y pixel relative to the window.
	 * @return {Node} An Element or Text node which i at the given coordinates.
	 */
	var _getRenderedNodeAtXY = document.elementFromPoint ?
	/* Use native version if available */
	function(x, y) {
		// return document.elementFromPoint(x, y);
		switch (_engine) {
		case _Platform.GECKO:
		case _Platform.TRIDENT:
			return document.elementFromPoint(x, y);

		default:
			// Webkit / presto requires page coordinates instead of
			// client/window coordinates
			var scrollPos = _getDocumentScrollPos();
			return document.elementFromPoint(x + scrollPos.left, y
					+ scrollPos.top); // Opera can return text nodes
		}

	} : function(x, y) {

		var element = null;

		searchElement(docBody);

		return element;

		function searchElement(parent) {

			// First search deeper nodes
			if (parent.childNodes.length > 0) {
				var child = parent.firstChild;
				while (child) {
					if (searchElement(child))
						return true;
					child = child.nextSibling;
				}

			}

			// Test this node... if it is an element
			if (parent.nodeType == Node.ELEMENT_NODE
					&& (parent.offsetLeft || parent.offsetLeft == 0)) {

				// Get the elements position in the window
				var pos = _getPositionInWindow(parent);

				// Then check to see if x/y is inside bounds
				if (y >= pos.y && y <= (pos.y + parent.offsetHeight)
						&& x >= pos.x && x <= (pos.x + parent.offsetWidth)) {
					// Search has finished
					element = parent;
					return true;
				}

			}

			return false;

		}

	};

	/**
	 * Gets that position of an element in the window. Supports internal scroll
	 * panes - price being slower operation.
	 * 
	 * @param {Object}
	 *            ele The element to get the position for.
	 * 
	 * @return {Object} The position of the given element {x,y}
	 */
	/*
	 * function _getPosInWndIntScrollSupport(ele){ var left = 0, top = 0, parent =
	 * ele;
	 * 
	 * do { if (parent.offsetLeft || parent.offsetTop) { left +=
	 * parent.offsetLeft; top += parent.offsetTop; } } while (parent =
	 * parent.offsetParent);
	 * 
	 * parent = ele; do { if (parent == docBody) break; // already handled in a
	 * cross-browser fashion if (parent.scrollLeft || parent.scrollTop) { left -=
	 * parent.scrollLeft; top -= parent.scrollTop; } } while (parent =
	 * parent.parentNode); // Notice here: going up parent nodes, not offsets //
	 * Get the document scroll var scrollPos = _getDocumentScrollPos();
	 *  // Return coordinates relative to window (using scroll information)
	 * return { x: left - scrollPos.left, y: top - scrollPos.top }; }
	 */

	/**
	 * Gets the position of an element in the window. Does not garuantee to
	 * support internal scrolls. However does support main document scrolling.
	 * 
	 * @param {Node}
	 *            ele The element to get the position for.
	 * 
	 * @return {Object} The position of the given element {x,y}
	 */
	function _getPosInWndFast(ele) {

		var left = 0, top = 0, isFixed = 0; // True if encountered fixed element

		do {

			// Add pixel offset to parent
			if (ele.offsetLeft || ele.offsetTop) {

				if (ele == docBody) {
					if (!isFixed) {
						// Gecko browsers can have negitive offsets for the
						// document body
						// if there is a border present.
						left += Math.abs(ele.offsetLeft);
						top += Math.abs(ele.offsetTop);
					}
				} else {
					left += ele.offsetLeft;
					top += ele.offsetTop;
				}

			}

			// TODO: NEED TO COMPUTE IF FIXED VIA CLASS.. expensive to do every
			// element...
			isFixed |= (ele.style && ele.style.position == "fixed");

		} while (ele = ele.offsetParent);

		if (!isFixed) {

			// Subtract the document scroll for non-fixed elements
			var scrollPos = _getDocumentScrollPos();

			left -= scrollPos.left;
			top -= scrollPos.top;

			// Observations:
			// IE Versions which already includes body border widths
			// IE8 Standards/Quirks
			// IE7 Quirks

			// IE Versions which do not include body border widths
			// IE7 Standards
			// IE 6 Standards
			if (_engine == _Platform.TRIDENT && _engineVersion < 8) {

				// Some IE verions do not add the body border in any of the
				// offsets (body/immediate children).
				// To get the border thicknesses in IE you can query the client
				// top/left which will not
				// be effected by scrollbars or margins.

				left += docBody.clientLeft;
				top += docBody.clientTop;
			}

		}

		// Return coordinates relative to window
		return {
			x : left,
			y : top
		};
	}

	/**
	 * @type Function
	 * 
	 * Gets the position of an element in the window.
	 * 
	 * @param {Node}
	 *            ele The element to get the position for.
	 * 
	 * @return {Object} The position of the given element {x,y}
	 */
	var _getPositionInWindow = _getPosInWndFast;

	/**
	 * Inserts a specified DOM node after a reference element as a child of the
	 * reference element's parent node.
	 * 
	 * @param {Node}
	 *            newNode The dom node being inserted
	 * 
	 * @param {Node}
	 *            refNode The node after which newNode is inserted.
	 * 
	 * @return {Node} newNode passed in
	 */
	function _insertAfter(newNode, refNode) {
		var sib = refNode.nextSibling;
		if (!sib)
			refNode.parentNode.appendChild(newNode);
		else
			refNode.parentNode.insertBefore(newNode, sib);
		return newNode;
	}

	/**
	 * Inserts a dom node at a given index.
	 * 
	 * @param {Node}
	 *            parentNode The parent of the newly inserted node
	 * @param {Node}
	 *            newNode The dom node being inserted
	 * @param {Number}
	 *            index The zero-based index of where in the parents child list
	 *            the new node should be added.
	 */
	function _insertAt(parentNode, newNode, index) {

		var i = -1;
		var node = parentNode.firstChild;

		while (++i != index && node) {
			node = node.nextSibling;
		}

		if (i == index) {
			if (i == parentNode.childNodes.length)
				parentNode.appendChild(newNode);
			else
				parentNode.insertBefore(newNode, node);

			return newNode;
		}

		return null;
	}

	/**
	 * Returns the combined length of all the descendant text nodes of a given
	 * element
	 * 
	 * @param {Node}
	 *            ele A element to get the text length for
	 * @return {Number} The text length for the given element
	 */
	function _getDeepTextLength(ele) {
		var len = 0;
		_visitTextNodes(ele, true, function(textNode) {
			len += _nodeLength(textNode);
		});
		return len;
	}

	/**
	 * Clones all object members.
	 * 
	 * @param {Object}
	 *            obj An object to clone
	 * 
	 * @return {Object} The cloned object.
	 */
	function _clone(obj) {
		var clone = {};
		for ( var i in obj)
			clone[i] = obj[i];
		return clone;
	}

	/**
	 * Traverses through DOM nodes and applies/maps a function to all nodes
	 * 
	 * @param {Node}
	 *            root The parent to search from. Inclusive in search. Null to
	 *            find the root automatically, up to and excluding the document
	 *            node (if any).
	 * 
	 * @param {Node}
	 *            start The node at which the traversal should start from. This
	 *            must be a child of parent, or the same as parent.
	 * 
	 * @param {Boolean}
	 *            searchRight True to traverse tree preorder from left to right,
	 *            e.g. current, child1, child2, ... False to traverse tree
	 *            postorder from right to left, e.g. ... child2, child1, current
	 * 
	 * @param {RegExp}
	 *            filter A regular expression - the function is only applied to
	 *            nodes whos names match the regular expression. If null is
	 *            supplied then all nodes are visited.
	 * 
	 * @param {Function}
	 *            func The function to apply. One argument is given: the visting
	 *            nodes. Returning false aborts traversal. Returning 1 in right
	 *            searches skips traversing into the current node's children.
	 *            Anything else returned will be ignored and the traversal will
	 *            continue.
	 */
	function _visitNodes(root, start, searchRight, filter, func) {

		// Ensure that root is set.
		if (!root)
			root = _getRoot(start, [ Node.DOCUMENT_NODE,
					Node.DOCUMENT_FRAGMENT_NODE ]);

		var startStack = _getAncestors(start, root, true, true);
		var stackIndex = startStack.length - 1;

		(function trav(parent) {

			var child, res, skipChildren = false;

			// Are we recursing to the starting node (building stack frame)?
			if (stackIndex > 0) { // before start point

				stackIndex--;
				child = searchRight ? startStack[stackIndex].nextSibling
						: startStack[stackIndex].previousSibling;
				if (!trav(startStack[stackIndex]))
					return false;

			} else if (stackIndex == 0) { // start point onwards

				// Map the function to the parent node if its node name isn't
				// filtered out
				if (searchRight && (!filter || filter.test(_nodeName(parent)))) {
					res = func(parent);
					if (res === false)
						return res;
					skipChildren = (res === 1); // Skip children?
				}

				// If we are traverse backwards (postorder + reverse sequence),
				// then
				// dont traverse deeper from the start node... begin moving
				// left/upward
				if (!searchRight && parent == start)
					child = null;
				else
					child = searchRight ? parent.firstChild : parent.lastChild;
			}

			// Search children
			if (!skipChildren) {
				while (child) {
					if (!trav(child))
						return false;
					child = (searchRight) ? child.nextSibling
							: child.previousSibling;
				}
			}

			// Map the function to the parent node if its node name isn't
			// filtered out
			if (!searchRight && (!filter || filter.test(_nodeName(parent))))
				if (func(parent) === false)
					return false;

			return true;

		})(root);

	}

	/**
	 * Traverses through DOM nodes and applies/maps a function to text nodes.
	 * 
	 * The tree traversal is preorder.
	 * 
	 * @param {Node}
	 *            root The parent to search from. Inclusive in search. Null to
	 *            find the root automatically.
	 * 
	 * @param {Node}
	 *            start The node at which the traversal should start from.
	 * 
	 * @param {Boolean}
	 *            searchRight True to traverse tree preorder from left to right,
	 *            false to traverse from right to left.
	 * 
	 * @param {Function}
	 *            func The function to apply. One argument is given: the child
	 *            text nodes. Returning false aborts traversal.
	 * 
	 * 
	 */
	function _visitTextNodes(root, start, searchRight, func) {
		_visitNodes(root, start, searchRight, /^#text$/, func);
	}

	/**
	 * Traverses through DOM nodes and applies/maps a function to all nodes.
	 * 
	 * The tree traversal is preorder.
	 * 
	 * @param {Node}
	 *            root The parent to search from. Inclusive in search. Null to
	 *            find the root automatically.
	 * 
	 * @param {Node}
	 *            start The node at which the traversal should start from.
	 * 
	 * @param {Boolean}
	 *            searchRight True to traverse tree preorder from left to right,
	 *            false to traverse from right to left.
	 * 
	 * @param {Function}
	 *            func The function to apply. One argument is given: the child
	 *            nodes. Returning false aborts traversal.
	 */
	function _visitAllNodes(root, start, searchRight, func) {
		_visitNodes(root, start, searchRight, null, func);
	}

	/**
	 * Determines whether a node is an ansetor of another.
	 * 
	 * @param {Node}
	 *            ancestor A dom node
	 * 
	 * @param {Node}
	 *            descendant A dom node
	 * 
	 * @return {Boolean} True if ancestor is an ancestor of descendant
	 */
	function _isAncestor(ancestor, descendant) {
		descendant = descendant.parentNode;
		while (descendant) {
			if (descendant == ancestor)
				return true;
			descendant = descendant.parentNode;
		}

		return false;
	}

	/**
	 * Gets ancestors of a dom node.
	 * 
	 * @param {Node}
	 *            child The node to get ancestors for.
	 * 
	 * @param {Node}
	 *            endAncestor The last ancestor of the search. This can be null
	 *            to get all ancestors
	 * 
	 * @param {Boolean}
	 *            includeChild True to include the child in with the ancestors.
	 * 
	 * @param {Boolean}
	 *            includeEndAncestor True to include the endAncestor in with the
	 *            ancestors.
	 * 
	 * @return {Array} An array of dom Node's containinhg the ancestors. Ordered
	 *         from child to ancestor
	 */
	function _getAncestors(child, endAncestor, includeChild, includeEndAncestor) {

		if (child == endAncestor)
			return (includeChild || includeEndAncestor) ? [ child ] : [];

		var ancestors = includeChild ? [ child ] : [];

		var nd = child.parentNode;

		while (nd && nd != endAncestor) {
			ancestors.push(nd);
			nd = nd.parentNode;
		}

		if (includeEndAncestor && endAncestor && nd == endAncestor)
			ancestors.push(endAncestor);

		return ancestors;
	}

	/**
	 * Finds an ancestor for a child up to a given point with a specific
	 * condition.
	 * 
	 * @example
	 * 
	 * var firstOccuringBlock = _findAncestor(child, docBody,
	 * de.html.isBlockLevel, true);
	 * 
	 * @param {Node}
	 *            child The child node to begin search from (Inclusive)
	 * 
	 * @param {Node}
	 *            endAncestorEx (Optional) The ancestor of the child node to
	 *            stop at, Null will search up to the dom tree root
	 * 
	 * @param {Function}
	 *            markFunc (Optional) A function which tests a given dom node.
	 *            Return true to mark the node for being the node to retrieve
	 *            (depending on stopOnFirst argument). False/null/undefined to
	 *            continue the search.
	 * 
	 * @param {Boolean}
	 *            stopOnFirst (Optional) True to stop the search on the first
	 *            encountered marked node, False/null/undefined to continue
	 *            search to find last occuring marked node in ancestor path.
	 * 
	 * @return {Node} The querried result - null if could not find
	 */
	function _findAncestor(child, endAncestorEx, markFunc, stopOnFirst) {

		var lastMarkedNode = null;

		while (child) {
			if (markFunc && markFunc(child)) {
				if (stopOnFirst)
					return child;
				lastMarkedNode = child;
			}
			if (child.parentNode == endAncestorEx)
				break;
			child = child.parentNode;
		}

		return markFunc ? lastMarkedNode : child;
	}

	/**
	 * Gets the first common ancestor between two nodes.
	 * 
	 * @param {Node}
	 *            node1 A dom node
	 * 
	 * @param {Node}
	 *            node2 A dom node
	 * 
	 * @param {Boolean}
	 *            inclusive True to count node1 and node2 as being a possible
	 *            common ancestor.
	 * 
	 * @return {Node} the first common ancestor between two nodes. Null if they
	 *         share no ancestor.
	 */
	function _getCommonAncestor(node1, node2, inclusive) {

		var ancestors1 = _getAncestors(node1, null, inclusive, 1), ancestors2 = _getAncestors(
				node2, null, inclusive, 1), commonParent = null;

		for ( var i in ancestors1) {
			for ( var j in ancestors2) {
				if (ancestors1[i] == ancestors2[j]) {
					return ancestors1[i];
				}
			}
		}

		return null;
	}

	/**
	 * Gets the next node in the preorder/postorder traversal.
	 * 
	 * @param {Node}
	 *            node The reference point.
	 * 
	 * @param {Boolean}
	 *            searchRight True to traverse tree preorder from left to right,
	 *            false to traverse postorder from right to left.
	 * 
	 * @return {Node} The next node. Null if none exists.
	 * 
	 */
	function _nextNode(node, searchRight) {
		var next = null;

		_visitNodes(null, node, searchRight, null, function(nd) {
			if (nd == node)
				return true; // Skip starting node
			next = nd;
			return false;
		});

		return next;
	}

	/**
	 * @param {Node}
	 *            node The node to get the root for. Must not be null.
	 * 
	 * @param {[Number]}
	 *            untilNodeTypes Optional, an aray of DOM Node constants. If
	 *            given, the search for the root node will stop just before the
	 *            first encountered given node type. For example.
	 *            [Node.DOCUMENT_NODE] will retreive up to the body element if
	 *            it has a document node ancestor.
	 * 
	 * @return {Node} the root of the given node
	 */
	function _getRoot(node, untilNodeTypes) {

		while (node.parentNode) {
			if (untilNodeTypes) {
				for ( var i in untilNodeTypes) {
					if (node.parentNode.nodeType == untilNodeTypes[i])
						return node;
				}
			}
			node = node.parentNode;
		}
		return node;
	}

	/**
	 * Gets a dom nodes child index in its parents childrens node list
	 * 
	 * @param {Node}
	 *            node A dom node
	 * 
	 * @return {Number} The zero-based index of where the node occurs in its
	 *         parent chilren. -1 if has no parent
	 */
	function _indexInParent(node) {
		var index = -1;
		while (node) {
			index++;
			node = node.previousSibling;
		}
		return index;
	}

	/**
	 * Returns a string so that special reserved entities and white spaces are
	 * escaped. Note that only whitespaces are escaped if they need to be....
	 * 
	 * @param {String}
	 *            text The text to escape.
	 * 
	 * @param {Boolean}
	 *            breakNewLines True to replace newline charactors with line
	 *            breaks. False to treat as whitespace
	 * 
	 * @return {String} The escaped version of text.
	 */
	function _escapeTextToHTML(text, breakNewLines) {

		var escapedText = "";
		var start = 0;
		var c, i;

		for (i = 0; i < text.length; i++) {
			c = text.charAt(i);

			var escapedStr = null;

			switch (c) {
			case "\"":
				escapedStr = "&quot;";
				break;
			case "'":
				escapedStr = "&#39;"; // &apos; does not work in IE
				break;
			case "&":
				escapedStr = "&amp;";
				break;
			case "<":
				escapedStr = "&lt;";
				break;
			case ">":
				escapedStr = "&gt;";
				break;
			default:
				if (breakNewLines && c == "\n") {
					escapedStr = "<br>";

				} else if (_isAllWhiteSpace(c)
						&& (i == 0 || i == (text.length - 1)
								|| text.charAt(i - 1) == " " || text
								.charAt(i + 1) == " ")) {
					escapedStr = "&nbsp;";
				}
			}

			// Does this charactor need escaping?
			if (escapedStr) {
				// First append charactors that are previously ok
				if ((i - start) > 0) {
					escapedText += (text.substring(start, i));
				}

				// Add the escaped version
				escapedText += escapedStr;

				// reset the start
				start = i + 1;
			}
		}

		// Add remaining text
		if ((i - start) > 0) {
			escapedText += (text.substring(start, i));
		}

		return escapedText;

	}

	/**
	 * 
	 * @param {String}
	 *            htmlText The html string to parse
	 * 
	 * @return {String} The escaped version of text.
	 */
	function _parseHTMLString(htmlText) {

		var tmp = $createElement("span");
		tmp.innerHTML = htmlText;
		return tmp.firstChild.nodeValue;

	}

	/**
	 * Determines whether a node is displayed or not depending on its immediate
	 * or inherited CSS display style. Note, that if a node's visibility is
	 * hidden, is does not mean it is not displayed.
	 * 
	 * @param {Node}
	 *            node A dom node to test
	 * 
	 * @return {Boolean} True if the dom node is displayed, false if it is not.
	 */
	function _isNodeDisplayed(node) {
		while (node) {
			if (node.nodeType == Node.ELEMENT_NODE) {
				if (node.style.display == "none")
					return false;
			}
			node = node.parentNode;
		}
		return true;
	}

	/**
	 * Retreives a CSS style directly set for or inherited by a given dom node.
	 * 
	 * @see www.quirksmode.org/dom/getstyles.html
	 * 
	 * @param {Node}
	 *            node A dom node.
	 * @param {String}
	 *            styleProp A CSS style property, formatted in CSS notation.
	 * @return {String} The inherited style of the given node. Undefined if the
	 *         node does not have the style. If the node is not an element, then
	 *         the first ancestor element is selected.
	 * 
	 */
	function _getComputedStyle(node, styleProp) {

		while (node && node.nodeType != Node.ELEMENT_NODE) {
			node = node.parentNode;
		}
		if (!node)
			return;

		if (window.getComputedStyle) // DOM Spec
			return document.defaultView.getComputedStyle(node, "")
					.getPropertyValue(styleProp)

		else if (node.currentStyle) // MS HTML
			return node.currentStyle[_styleCSSToJSNotation(styleProp)];

		debug.println("Warning - could not get style \"" + styleProp
				+ "\" for a \"" + node.nodeName + "\" element");
		// Otherwise undefined...
	}

	/**
	 * Sets an element's CSS string
	 * 
	 * @param {Node}
	 *            ele An element node
	 * @param {String}
	 *            css A css style string formatted in CSS notation.
	 * 
	 * @example _setFullStyle(myElement, "color:red; padding:4px;
	 *          font-size:12px");
	 */
	function _setFullStyle(ele, css) {
		if (_engine == _Platform.TRIDENT)
			ele.style.setAttribute("cssText", css);
		else
			ele.setAttribute("style", css);
	}

	/**
	 * Sets a CSS style value for a given element
	 * 
	 * @param {Node}
	 *            ele An element node
	 * 
	 * @param {String}
	 *            css The CSS style to set in JS Notation
	 * 
	 * @param {String}
	 *            val The value of the new style
	 */
	function _setStyle(ele, css, val) {
		if (_engine == _Platform.TRIDENT)
			ele.style.setAttribute(css, val);
		else
			ele.style[css] = val;
	}

	/**
	 * Retrieves the full CSS markup for an elements style. Note that this is
	 * not the computed markup - it is the explicitely assigned CSS for the
	 * particular node.
	 * 
	 * @param {Node}
	 *            ele The element to get the style from
	 * @return {String} The CSS for the given element. Never null, empty if no
	 *         explicit styles set
	 */
	function _getFullStyle(ele) {
		return (_engine == _Platform.TRIDENT ? ele.style
				.getAttribute("cssText") : ele.getAttribute("style"))
				|| "";
	}

	/**
	 * @param {Element}
	 *            ele The element to check
	 * @return {Boolean} Evaluates to true iff the element has an element-level
	 *         style (i.e not computed).
	 */
	function _doesHaveElementStyle(ele) {

		var fs = _getFullStyle(ele);

		// Check if the CSS text contains non-empty style-values
		if (fs) {
			fs = fs.split(";");
			for ( var s in fs) {
				var idx = fs[s].indexOf(':');
				if (idx > 0 && idx < (fs[s].length - 1)
						&& /\s*\S+\s*/.test(fs[s].substr(idx)))
					return 1;
			}
		}
	}

	/**
	 * @param {String}
	 *            styleProp A CSS style in JS notation.
	 * @return {String} The given CSS style in CSS notation.
	 */
	function _styleJSToCSSNotation(styleProp) {
		do {
			var match = /([A-Z])/.exec(styleProp);
			if (match)
				styleProp = styleProp.substr(0, match.index) + "-"
						+ match[1].toLowerCase()
						+ styleProp.substr(match.index + 1);
		} while (match);

		return styleProp;
	}
	;

	/**
	 * @param {String}
	 *            color A CSS Style color. Can be in hex, rgb, percentages or
	 *            actual names. NOTE: Only supports converting the 16
	 *            standardized HTML color names - unstandard color names will
	 *            return white.
	 * 
	 * @return {[Number]} An array with elements R,G and B respectively. They
	 *         range from 0-255.
	 * 
	 * DEPRECIATED
	 */
	var _getColorRGB = function() {

		var colorRegExp = /^\s*rgb\s*\(\s*(\d+)\%?\s*\,\s*(\d+)\%?\s*\,\s*(\d+)\%?\s*\)\s*$/i,

		/*
		 * Only going to support the 16 standardized colors. All major browsers
		 * support a lot more but will seriously bloat the api size.
		 */
		colorWMap = {
			maroon : [ 128, 0, 0 ],
			red : [ 255, 0, 0 ],
			orange : [ 255, 165, 0 ],
			yellow : [ 255, 255, 0 ],
			olive : [ 128, 128, 0 ],
			purple : [ 128, 0, 128 ],
			fuchsia : [ 255, 0, 255 ],
			white : [ 255, 255, 255 ],
			lime : [ 0, 255, 0 ],
			green : [ 0, 128, 0 ],
			navy : [ 0, 0, 128 ],
			blue : [ 0, 0, 255 ],
			aqua : [ 0, 255, 255 ],
			teal : [ 0, 128, 128 ],
			black : [ 0, 0, 0 ],
			silver : [ 12, 12, 12 ],
			gray : [ 128, 128, 128 ]
		};

		return function(val) {

			if (val.charAt(0) == "#") {
				if (val.length < 7)
					val += "000000";
				// Convert to RGB
				return [ parseInt(val.substr(1, 2), 16),
						parseInt(val.substr(3, 2), 16),
						parseInt(val.substr(5, 2), 16) ];
			}

			// Is the color in the notation "rbg(r,b,g)" ?
			var match = colorRegExp.exec(val);
			if (match) {

				var r = parseInt(match[1]), g = parseInt(match[2]), b = parseInt(match[3]);

				if (val.indexOf("%") > -1) { // convert percentages to 255
												// range
					if (r > 100)
						r = 100; // clamp;
					r = (255 * r) / 100;
					if (g > 100)
						g = 100; // clamp;
					g = (255 * g) / 100;
					if (b > 100)
						b = 100; // clamp;
					b = (255 * b) / 100;
				} else { // Clamp 255 range
					if (r > 255)
						r = 255;
					if (g > 255)
						g = 255;
					if (b > 255)
						b = 255;
				}

				return [ r, g, b ];
			}

			return colorWMap[val.toLowerCase()] || [ 255, 255, 255 ];
		}

	}();

	/**
	 * @param {String}
	 *            cssStyle A CSS Style to check
	 * @param {String}
	 *            val1 The value of a CSS style to compare (with val2)
	 * @param {String}
	 *            val2 The value of a CSS style to compare (with val1)
	 * @return {Boolean} True if val1 is equivalent to val2 CSS
	 * 
	 * DEPRECIATED
	 * 
	 */
	var _isCSSValueSame = function() {

		var fontWeightMap = {
			bold : "700",
			normal : "400"
		};

		function normalizeFontWeight(val) {
			return fontWeightMap[val.toLowerCase()] || val;
		}

		return function(cssStyle, val1, val2) {

			switch (cssStyle) {
			case "backgroundColor":
			case "borderColor":
			case "outlineColor":
			case "color":
				val1 = _getColorRGB(val1);
				val2 = _getColorRGB(val2);
				return val1[0] == val2[0] && val1[1] == val2[1]
						&& val1[2] == val2[2];

			case "fontWeight":
				val1 = normalizeFontWeight(val1);
				val2 = normalizeFontWeight(val2);
				break;
			}

			return val1 == val2;

		};

	}();

	/**
	 * @param {String}
	 *            styleProp A CSS style in CSS notation.
	 * @return {String} The given CSS style in JS notation.
	 */
	function _styleCSSToJSNotation(styleProp) {
		do {
			var index = styleProp.indexOf("-");
			if (index > -1)
				styleProp = (index == (styleProp.length - 1)) ? styleProp
						.substr(0, index) : styleProp.substr(0, index)
						+ styleProp.charAt(index + 1).toUpperCase()
						+ styleProp.substr(index + 2);
		} while (index > -1);
		return styleProp;
	}
	;

	/**
	 * Gets the outer HTML content of a given element. NOTE: Can be expensive
	 * for large DOM Trees in firefox/konqueror.
	 * 
	 * @param {Node}
	 *            node An Element to get it's outer HTML for.
	 * @return {String} The outer html of the given node.
	 */
	function _getOuterHTML(node) {
		if (node.outerHTML)
			return node.outerHTML;
		else { // Firefox / konqueror
			var tmp = $createElement("span");
			tmp.appendChild(node.cloneNode(true));
			return tmp.innerHTML;
		}
	}

	/**
	 * @return {Boolean} True if this browser allows you to safely extend the
	 *         DOM.
	 */
	function _isDOMExtendable() {
		/* IE Versions 7 down are not core javascript. */
		return !(_browser == _Platform.IE && _browserVersion < 8);
	}

	/**
	 * @param {Node}
	 *            node The node to extract the class name from
	 * @param {RegExp}
	 *            A regular expression.
	 * @return {String} the first occurring classname of the node which matches
	 *         regexp. Null if did not find a match
	 */
	function _findClassName(node, regexp) {
		if (node.nodeType == Node.ELEMENT_NODE || node == docBody) {
			var clsName = _getClassName(node);
			if (clsName) {
				var classNames = clsName.split(' ');
				for ( var i in classNames) {
					if (regexp.test(classNames[i]))
						return classNames[i];
				}
			}
		}
		return null;
	}

	/**
	 * @param {Node}
	 *            element A Dom element
	 * @return {String} The class name for the given element
	 */
	function _getClassName(element) {
		return element.className;
	}

	/**
	 * @param {Node}
	 *            element A Dom element
	 * @param {String}
	 *            name The class to set - overrides all classes.
	 */
	function _setClassName(element, name) {
		return _browser == _Platform.IE ? element.setAttribute("className",
				name) : element.className = name;
	}

	/**
	 * Not all browsers support Array.indexOf .. this is a manual impl.
	 * 
	 * @param {Object}
	 *            obj An object
	 * @param {Array}
	 *            arr An array
	 * @return {Number} The index of obj in arr. -1 if obj is not in arr.
	 */
	function _indexOf(obj, arr) {
		for ( var i in arr) {
			if (arr[i] == obj)
				return parseInt(i);
		}
		return -1;
	}

	/**
	 * @param {Node}
	 *            node a dom node
	 * @return {String} The dom node's name in lower case
	 */
	function _nodeName(node) {
		return node.nodeName.toLowerCase();
	}

	/**
	 * Determines whether a node is a text node and returns the text length if
	 * it is.
	 * 
	 * @param {Node}
	 *            node The dom node to test
	 * 
	 * @param {Object}
	 *            defaultValue (optional) If the node to test is not a text node
	 *            then this value will be returned instead. Defaults to NULL.
	 * 
	 * @return {Object} If the node is a text node, then the text length of the
	 *         node will be returned. Otherwise defaultValue will be returned.
	 */
	function _nodeLength(node, defaultValue) {
		if (typeof defaultValue == "undefined")
			defaultValue = null;
		return node.nodeType == Node.TEXT_NODE ? node.nodeValue.length
				: defaultValue;
	}

	/**
	 * Determines if an object is a DOM Node or not.
	 * 
	 * @param {Object}
	 *            obj The object to test
	 */
	function _isDOMNode(obj) {

		// @DEBUG ON
		// In debug mode, the Node object will be created if it is not available
		// - in order to provide
		// node type constants. To distinuish from a real node object and the
		// fabricaed one, test if the
		// _DE_DEBUG_CREATED if there
		if (Node._DE_DEBUG_CREATED)
			return typeof obj == "object" && typeof obj.nodeType == "number"
					&& typeof obj.nodeName == "string";
		// @DEBUG OFF

		return typeof Node == "object" ? obj instanceof Node
				: (typeof obj == "object" && typeof obj.nodeType == "number" && typeof obj.nodeName == "string");
	}
	;

	/*
	 * Expose internals to public
	 */
	$extend(de, {

		/**
		 * Exposure of _visitAllNodes internal
		 * 
		 * @see _visitAllNodes
		 */
		visitAllNodes : _visitAllNodes,

		getCommonAncestor : _getCommonAncestor,

		/**
		 * @param {Node}
		 *            node a dom node
		 * @return {String} The inner text of the given node, never null, but
		 *         can be empty.
		 */
		getInnerText : function(node) {
			if (node.nodeType == Node.TEXT_NODE)
				return node.nodeValue;
			return node.innerText || node.textContent || "";
		},

		/**
		 * Exposure of _parseHTMLString internal
		 * 
		 * @see _parseHTMLString
		 */
		parseHTMLString : _parseHTMLString,

		/**
		 * Exposure of _insertAfter internal
		 * 
		 * @see _insertAfter
		 */
		insertAfter : _insertAfter,

		/**
		 * Exposure of _insertAt internal
		 * 
		 * @see _insertAt
		 */
		insertAt : _insertAt,

		/**
		 * Exposure of _findClassName internal
		 * 
		 * @see _findClassName
		 */
		findClassName : _findClassName,

		/**
		 * Exposure of _getPositionInWindow internal
		 * 
		 * @see _getPositionInWindow
		 */
		getPositionInWindow : _getPositionInWindow,

		getOuterHTML : _getOuterHTML,

		getComputedStyle : _getComputedStyle

	});
	// END Util.js
	// START Changes.js
	(function() {

		/*
		 * All editable sections and their initial states since startup or the
		 * last clear.
		 */
		var esStartStates = [];

		$enqueueInit("Changes", function() {

			de.Changes.clear();
			de.doc.addObserver({
				onSectionAdded : function(editSection) {

					// Safety check: make sure editSection is not already
					// registered
					// @DEBUG ON
					for ( var i in esStartStates) {
						debug.assert(esStartStates[i].esNode != editSection);
					}
					// @DEBUG OFF

					// Add the new section to the state array
					esStartStates.push({
						esNode : editSection,
						initHTML : editSection.innerHTML
					});

				}
			});

		}, "Doc");

		/**
		 * @class A singleton that records the editable sections that have been
		 *        changed/added/removed over time
		 */
		de.Changes = {

			/**
			 * Gets all changes since last clear
			 * 
			 * @return {[Element]} A list of changed editable section nodes.
			 *         NOTE: Does not include removed editable sections .. it
			 *         only checks the editable section contents.
			 * 
			 * @see de.Changes.clear
			 */
			getChangedEditableSections : function() {

				var changedSections = [], stipEmptiesRE = /(<\s*\w+\s[^>]*?)(?:style|class|id|value)\s*=\s*(?:""|'')([^<]*?>)/i, stipEmptiesREPresto = /(<\s*\w+\s[^>]*?)\s*(?:style|class|id|value)\s*(>|(?:[^=][^<]*?>))/i, stripAttribWSRE = /<[^\/][^<>]*?\s[^<>]*>/, wsRE = /(?:[\t\n\r ]|&nbsp;)/g;

				// Don't consider highlighting as part of HTML
				_toggleSectionHighlight(false);

				// Look for changes
				for ( var i in esStartStates) {
					var esSection = esStartStates[i];
					if (stripIrrelevants(esSection.esNode.innerHTML) != stripIrrelevants(esSection.initHTML)
							|| esSection.dirty)
						changedSections.push(esSection.esNode);
				}

				_toggleSectionHighlight(true);

				return changedSections;

				/**
				 * Strip irrelevent html from markup when comparing differences.
				 * E.G. Empty attibutes or different whitespace encodings.
				 * 
				 * @param {String}
				 *            str The html to stip irrelevent data from
				 */
				function stripIrrelevants(str) {

					// Make all whitespaces normal whitespace
					str = str.replace(wsRE, " ");

					var match, i, re, newStr = "";

					// Strip empty attributes
					// One regexp matching pass for all browsers except for
					// opera...
					// Opera can leave empty attrbiutes without ="".
					for (i = 0; i < (_engine == _Platform.PRESTO ? 2 : 1); i++) {

						// Select the regexp according to pass
						re = i == 0 ? stipEmptiesRE : stipEmptiesREPresto;

						while (match = re.exec(str)) {
							str = str.substr(0, match.index) + match[1]
									+ match[2]
									+ str.substr(match.index + match[0].length);
						}

					}

					if (match) {

						// Due to attributes from being stripped must clear
						// whitespaces which separate attibutes in html tags..
						// since whitespaces may only be present for the empty
						// tags
						while (match = stripAttribWSRE.exec(str)) {
							newStr += str.substr(0, match.index)
									+ match[0].replace(wsRE, "");
							str = str.substr(match.index + match[0].length);
						}
						newStr += str;

					} else
						newStr = str;

					return de.spell.stripSpellWrapperHTML(newStr);
				}

			},

			/**
			 * Wipes all recorded changes and prepares for recording new changes
			 * for all or a specific edit section.
			 * 
			 * @param {Element}
			 *            es (Optional) The edit section to wipe. If not
			 *            provided then all edit sections will be wiped
			 * 
			 * @see de.Changes.reset
			 */
			clear : function(es) {

				// Exclude highlighting in HTML snapshots
				_toggleSectionHighlight(false);

				if (es) {

					// Locate specific editable section to wipe changes
					for ( var i in esStartStates) {
						if (esStartStates[i].esNode == es) {
							esStartStates[i].initHTML = es.innerHTML;
							esStartStates[i].dirty = 0;
							break;
						}
					}

				} else {

					// Wipe all previous state information
					esStartStates = [];

					// Build up the state information based on the current
					// document state
					var editableSections = de.doc.getAllEditSections();

					for ( var i in editableSections) {
						var domNode = editableSections[i];

						esStartStates.push({
							esNode : domNode,
							initHTML : domNode.innerHTML
						});
					}
				}

				_toggleSectionHighlight(true);
			},

			/**
			 * Use to mark all or a specific editable section as being dirty.
			 * 
			 * If marked as dirty, then the next request for changed editable
			 * sections will also return the editable section marked dirty even
			 * if they have not changed / have been cleared.
			 * 
			 * The next time the edit section is cleared it will be unmarked as
			 * being dirty.
			 * 
			 * @param {Object}
			 *            es (Optional) The edit section to dirty. If not
			 *            provided then all edit sections will be made dirty.
			 */
			dirty : function(es) {

				for ( var i in esStartStates) {
					if (!es || esStartStates[i].esNode == es) {
						esStartStates[i].dirty = 1;
					}
				}

			},

			/**
			 * Clears the changes, and resets all edit sections html to their
			 * initial state since start up or the last clear/reset operation.
			 * All undo/redo history will be cleared;
			 * 
			 * @see de.Changes.clear
			 */
			reset : function() {

				// Clear undo/redo history
				de.UndoMan.clear();

				// Reset edit section html back to their last captured start
				// states
				for ( var i in esStartStates) {
					esStartStates[i].esNode.innerHTML = esStartStates[i].initHTML;
				}

				// Setup changes again
				this.clear();
			}

		}; // End de.Changes singleton

	})();
	// END CHANGES.js
	// Start Clipboard.js

	(function() {

		$enqueueInit(
				"Clipboard",
				function() {

					// Setup platform independant event handlers for Accel+C,
					// Accel+V and Accel+X key strokes
					switch (_engine) {
					case _Platform.TRIDENT:
						_addHandler(document, "keydown", onIEKeyDown);
						break;

					case _Platform.GECKO:
						_addHandler(document, "keypress", onGeckoKeyPress);
						break;

					case _Platform.PRESTO:
						_addHandler(document, "keydown", onPrestoKeyDown);
						break;

					case _Platform.WEBKIT:
						_addHandler(document, "copy", onWKCopy);
						_addHandler(document, "paste", onWKPaste);
						_addHandler(document, "keydown", onWKKeyDown); // Cutting
																		// in
																		// all
																		// webkit,
																		// copy/paste
																		// in
																		// safari
																		// mac
						break;

					}

					// Create the multi-lined text box for capturing clipboard
					// ketstrokes
					clipInputEle = $createElement("textarea");
					_setFullStyle(clipInputEle,
							"width:1px;height:1px;border-style:none");
					clipContainer = $createElement("div");
					_setClassName(clipContainer, _PROTECTED_CLASS);
					_setFullStyle(clipContainer,
							"position:absolute;width:1px;height:1px;display:none;z-index:-500");

					clipContainer.appendChild(clipInputEle);
					docBody.appendChild(clipContainer);

				}, "events.Events");

		/* The internal clipboard text - stored whenever a user copies. */
		var intClipText,

		/* The internal clipboard DOM - stored whenever a user copies. */
		intClipDOM,

		/*
		 * True if managed to copy the current internal clip text to the system
		 * clipboard
		 */
		isSysClip,

		/* Used for copy/cut/paste keystroke hijacking. */
		clipInputEle,

		/* Used for copy/cut/paste keystroke hijacking. */
		clipContainer,

		/* Used for copy/cut/paste keystroke hijacking. */
		clipboardTOID = null,

		/*
		 * The cursor descriptor to restore to after a native copy. NULL if
		 * there is no cursor to restore.
		 */
		restoreCursor;

		/**
		 * Converts a DOM tree to a textual version.
		 * 
		 * @param {Node}
		 *            node A dom tree to convert to text
		 * @return {String} The text equivalent of the given root node of the
		 *         dom tree.
		 */
		function domToText(node) {

			var text = "", child;

			if (node.nodeType == Node.TEXT_NODE && _doesTextSupportNonWS(node)) {
				text = node.nodeValue.replace(/[\t\n\r]/g, " "); // Make all
																	// HTML-whitespace
																	// symbols
																	// actual
																	// whitespace

			} else if (node.nodeType == Node.ELEMENT_NODE) {
				switch (_nodeName(node)) {
				case "br":
					text += "\n";
					break;
				case "li":
					text += "\n * ";
					break;
				default:
					if (_isBlockLevel(node))
						text += "\n";
				}

				// Recurse
				child = node.firstChild;
				while (child) {
					text += domToText(child);
					child = child.nextSibling;
				}

				// Block-elements have line breaks before and after
				if (_isBlockLevel(node))
					text += "\n";
			}

			return text;
		}

		/**
		 * Copies the documents selection to the internal clipboard. Sets the
		 * locals intClipText, intClipDOM and isSysClip appropriatly if there is
		 * something to copy.
		 * 
		 * @return {String} The text that is copied to the internal clipboard.
		 *         Null if there was nothing to copy (the clipboard state will
		 *         be unchanged in this case).
		 */
		function internalCopy() {

			// Get the current document selections dom
			var selection = de.selection.getHighlightedDOM();
			if (!selection)
				return null;

			// Store duplicated dom
			intClipDOM = selection;

			// Special case: if the selection root is a list element then we
			// need to
			// get the list element type (ol/ul)

			// TODO

			// Convert DOM into text
			intClipText = domToText(intClipDOM);

			// Chop off leading and trailing new line if the dom tree's root is
			// block level
			if (_isBlockLevel(intClipDOM))
				intClipText = intClipText.replace(/^\n/, "").replace(/\n$/, "");

			// Reset system clip flag
			isSysClip = false;

			return intClipText;
		}

		/**
		 * Copies and removes the documents selection to the internal clipboard.
		 * 
		 * @return {String} The text that is copied to the internal clipboard.
		 *         Null if there was nothing to cut (the clipboard and document
		 *         state will be unchanged in this case).
		 * 
		 * @see internalCopy
		 */
		function internalCut() {

			var res = internalCopy();

			// If something was copied, remove any selection from the document
			if (res)
				de.selection.remove();

			return res;
		}

		/**
		 * Pastes text, or DOM, into the document, if the cursor is in an
		 * editable section.
		 * 
		 * @param {String}
		 *            sysClipText The textual contents of the system clipboard
		 *            if available.
		 */
		function internalPaste(sysClipText) {

			// Don't try paste if the cursor does not exist
			if (!de.cursor.exists())
				return;

			// Check permissions of cursor position....

			// Remove the current selection if any
			de.selection.remove();

			var cursorDesc = de.cursor.getCurrentCursorDesc();
			debug.assert(cursorDesc != null);

			var textToPaste, domToPaste;

			// Has there ever been anything copied internally in this session
			// before?
			if (intClipText) {

				// If the internal clipboard content was unable to be copied to
				// the system clipboard,
				// then unfortunatly we will have to use this.
				if (!isSysClip)
					domToPaste = intClipDOM;

				// If the internal clipboard text matches the system clipboard
				// text, then use the
				// DOM content since it is the riches content.
				else if (intClipText.replace(/\s/g, "") == sysClipText.replace(
						/\s/g, ""))
					domToPaste = intClipDOM;

				// If the system clipboard text is available, use that
				else if (sysClipText)
					textToPaste = sysClipText;

				// If all else fails, use the internal clip text
				else
					domToPaste = intClipDOM;

			} else
				textToPaste = sysClipText; // If available, use the text in the
											// system clipboard

			// TMP - for debugging text pasting etc..
			// if (domToPaste) {
			// domToPaste = null;
			// textToPaste = intClipText;
			// }

			// Is there anything to paste?
			if (domToPaste || textToPaste) {

				// Calculate the cursor index
				var index = cursorDesc.relIndex;
				if (cursorDesc.isRightOf)
					index++;
				if (_nodeName(cursorDesc.domNode) == "br")
					index = 1;

				var es = de.doc.getEditSectionContainer(cursorDesc.domNode);

				if (es) {

					var esProps = de.doc.getEditProperties(es);

					if (!esProps.singleLine && domToPaste) { // Can we paste
																// DOM content?

						// TODO, HTML validation, DEdit filters... should this
						// be in the insert HTML command?
						// LOW PRIORITY

						// This will take a lot of thought...

						// TEMP HACK: Just past inline HTML
						var inlineContentHolder = $createElement("div");

						_visitAllNodes(
								domToPaste,
								domToPaste,
								true,
								function(node) {

									// Add text nodes to inline content holder
									if (node.nodeType == Node.TEXT_NODE)
										inlineContentHolder.appendChild(node
												.cloneNode(false));

									else if (_isInlineLevel(node)
											&& _isValidRelationship(
													node,
													cursorDesc.domNode.parentNode)) {

										// If this node is inline and can be
										// validly inserted into char position,
										// see if all of its children are inline
										var isAllInline = 1;
										_visitAllNodes(
												node,
												node,
												true,
												function(innerNode) {

													// End search once exits
													// subtree
													if (!_isAncestor(node,
															innerNode))
														return false;

													// Is a node found to be
													// block level?
													if (_isBlockLevel(innerNode)) {
														isAllInline = 0;
														return false;
													}
												});

										// If all inline/text then copy this sub
										// tree
										if (isAllInline
												&& !(esProps.singleLine && _nodeName(node) == "br")) {
											inlineContentHolder
													.appendChild(node
															.cloneNode(true));
											return 1;
										}

									}

								});

						if (inlineContentHolder.firstChild)
							de.UndoMan.execute("InsertHTML",
									inlineContentHolder.innerHTML,
									cursorDesc.domNode.parentNode,
									cursorDesc.domNode, index);

					} else if (textToPaste) { // Can we paste text content?
						// Decide on insertion action and perform it
						if (!esProps.singleLine && /\n/.test(textToPaste)) // If
																			// has
																			// newlines
																			// then
																			// replace
																			// with
																			// line
																			// breaks
							de.UndoMan.execute("InsertHTML", _escapeTextToHTML(
									textToPaste, true),
									cursorDesc.domNode.parentNode,
									cursorDesc.domNode, index);
						else
							de.UndoMan.execute("InsertText",
									cursorDesc.domNode, textToPaste, index);

					}
				}
			}

		}

		/**
		 * IE Only. Attempts to copy the text to the clipboard.
		 * 
		 * @param {String}
		 *            text the text to copy
		 * @return {Boolean} True iff the text was successfully copied to the
		 *         system clipboard.
		 */
		function ieClipboardCopy(text) {
			var didSucceed = window.clipboardData.setData('Text', text);
			return didSucceed === $undefined || didSucceed;
		}

		/**
		 * IE Only.
		 * 
		 * @return {String} The system clipboard's text. Null if unavailable
		 */
		function ieClipboardRetrieve() {
			var clipText = window.clipboardData.getData('Text');
			if (clipText === "") { // Could be empty, or failed
				// Verify failure
				if (!window.clipboardData.setData('Text', clipText))
					clipText = null;
			}
			return clipText;
		}

		/**
		 * IE's On key down event
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onIEKeyDown(e) {
			e = e || window.event;
			if (!de.events.Keyboard.isAcceleratorDown(e))
				return;

			switch (e.keyCode) {
			case 67: // COPY (C)
			case 88: // CUT (X)

				// Perform internal copy
				var textToCopy = e.keyCode == 67 ? internalCopy()
						: internalCut();
				if (textToCopy) {
					// Try to copy the text to the system clipboard the IE way
					if (ieClipboardCopy(textToCopy))
						isSysClip = true;
					else
						fallThroughCopyEvent(textToCopy);

				}
				break;

			case 86: // PASTE (V)

				var sysClipContents = ieClipboardRetrieve();

				if (sysClipContents)
					internalPaste(sysClipContents);
				else
					fallThoughPasteEvent();

				break;

			}

		}

		/**
		 * For mozilla platforms only.
		 * 
		 * @return {Boolean} True iff this session has privileges to access
		 *         XPConnect resources
		 */
		function hasXPCPriv() {
			try {
				if (netscape.security.PrivilegeManager.enablePrivilege)
					netscape.security.PrivilegeManager
							.enablePrivilege("UniversalXPConnect");
				else
					return false;
			} catch (ex) {
				return false;
			}
			return true;
		}

		/**
		 * Mozilla Only. Attempts to copy the text to the clipboard.
		 * 
		 * @param {String}
		 *            text the text to copy
		 * @return {Boolean} True iff the text was successfully copied to the
		 *         system clipboard.
		 */
		function mozClipboardCopy(text) {

			try {

				if (!hasXPCPriv())
					return false;

				var str = Components.classes["@mozilla.org/supports-string;1"]
						.createInstance(Components.interfaces.nsISupportsString);
				str.data = text;

				var trans = Components.classes["@mozilla.org/widget/transferable;1"]
						.createInstance(Components.interfaces.nsITransferable);
				if (!trans)
					return false;

				trans.addDataFlavor("text/unicode");
				trans.setTransferData("text/unicode", str, copytext.length * 2);

				var clipid = Components.interfaces.nsIClipboard;
				var clip = Components.classes["@mozilla.org/widget/clipboard;1"]
						.getService(clipid);
				if (!clip)
					return false;

				clip.setData(trans, null, clipid.kGlobalClipboard);

			} catch (e) {
				// FF Sometimes throws random errors on blanks lines
				return false;
			}
		}

		/**
		 * Mozilla Only.
		 * 
		 * @return {String} The system clipboard's text. Null if unavailable
		 */
		function mozClipboardRetrieve() {

			try {

				if (!hasXPCPriv())
					return null;

				var clip = Components.classes["@mozilla.org/widget/clipboard;1"]
						.getService(Components.interfaces.nsIClipboard);
				if (!clip)
					return null;

				var trans = Components.classes["@mozilla.org/widget/transferable;1"]
						.createInstance(Components.interfaces.nsITransferable);
				if (!trans)
					return null;
				trans.addDataFlavor("text/unicode");

				clip.getData(trans, clip.kGlobalClipboard);

				var str = {}, strLength = {}, pastetext = "";

				trans.getTransferData("text/unicode", str, strLength);

				if (str)
					str = str.value
							.QueryInterface(Components.interfaces.nsISupportsString);

				if (str)
					pastetext = str.data.substring(0, strLength.value / 2);

				return pastetext;

			} catch (e) {
				// FF Sometimes throws random errors on blanks lines
				return null;
			}
		}

		/**
		 * Mozilla's On key press event
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onGeckoKeyPress(e) {

			if (!de.events.Keyboard.isAcceleratorDown(e))
				return;

			switch (e.which) {
			case 99: // COPY (C)
			case 67:
			case 120: // CUT (X)
			case 88:

				// Perform internal copy
				var textToCopy = (e.which == 67 || e.which == 99) ? internalCopy()
						: internalCut();
				if (textToCopy) {
					// Try to copy the text to the system clipboard the XUL way
					if (mozClipboardCopy(textToCopy))
						isSysClip = true;
					else {
						fallThroughCopyEvent(textToCopy);
					}
				}
				break;

			case 118: // PASTE (V)
			case 86:
				var sysClipContents = mozClipboardRetrieve();

				if (sysClipContents)
					internalPaste(sysClipContents);
				else
					fallThoughPasteEvent();

				break;

			}

		}

		/**
		 * Opera's On key down event
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onPrestoKeyDown(e) {
			if (!de.events.Keyboard.isAcceleratorDown(e))
				return;
			switch (e.keyCode) {
			case 67: // COPY (C)
				var textToCopy = internalCopy();
				if (textToCopy)
					fallThroughCopyEvent(textToCopy);
				break;
			case 88: // CUT (X)
				var textToCopy = internalCut();
				if (textToCopy)
					fallThroughCopyEvent(textToCopy);
				break;
			case 86: // PASTE (V)
				fallThoughPasteEvent();
				break;
			}
		}

		/**
		 * Webkit's on copy event
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onWKCopy(e) {
			// Webkit has a bug where the clipboard data cannot be set in the
			// clipboard
			// events, even though the specificatoin states that it can be set.
			// Therefore
			// must resort to fall-through event capturing for a workaround
			if (clipboardTOID === null) { // If not currently using
											// fall-through method...
				var textToCopy = internalCopy();
				if (textToCopy)
					fallThroughCopyEvent(textToCopy);
			}
		}

		/**
		 * Webkit's on paste event
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onWKPaste(e) {

			// clipboardData is available for access only in this event
			if (de.cursor.exists() && clipboardTOID === null) { // If not
																// currently
																// using
																// fall-through
																// method... and
																// something is
																// selected
				internalPaste(e.clipboardData.getData("Text"));
				e.preventDefault(); // NOTE: Only prevent default if pasting in
									// editable section, other allow pasting in
									// native controls
			}

		}

		/**
		 * Webkit's on key down event (Cutting only)
		 * 
		 * @param {Event}
		 *            e The dom event
		 */
		function onWKKeyDown(e) {
			if (de.events.Keyboard.isAcceleratorDown(e)) {

				switch (e.keyCode) {
				case 88: // X: Cut events in webkit dont work
					var textToCopy = internalCut();
					if (textToCopy)
						fallThroughCopyEvent(textToCopy);
					break;

				case 67: // C: Copy events via keyboard in safari mac dont
							// work
					if (_browser == _Platform.SAFARI && _os == _Platform.MAC
							&& de.cursor.exists()) {
						// Observation: Safari 4 on mac does not allow copy
						// events to occur if
						// not coping in native text controls.

						// Perform internal copy
						var textToCopy = internalCopy();
						if (textToCopy)
							fallThroughCopyEvent(textToCopy);

					}
					break;

				case 86: // V: Paste events via keyboard in safari mac dont
							// work
					if (_browser == _Platform.SAFARI && _os == _Platform.MAC
							&& de.cursor.exists())
						// Observation: Safari 4 on mac does not allow paste
						// events to occur if
						// not pasting in native text controls.
						fallThoughPasteEvent();

					break;

				}

			}
		}

		/**
		 * Invoked just before the browser is about to execute default/native
		 * code which copies the documents current native selection.
		 * 
		 * @param {String}
		 *            textToCopy The text to copy to the clipboard.
		 *            Null/undefined if pasting
		 */
		function fallThroughCopyEvent(textToCopy) {
			fallThoughClipEventBase(textToCopy);
		}

		function fallThoughPasteEvent() {
			fallThoughClipEventBase();
		}

		function fallThoughClipEventBase(textToCopy) {

			restoreCursor = de.cursor.getCurrentCursorDesc();

			// Avoid race conditions with pending timeout
			if (clipboardTOID)
				clearTimeout(clipboardTOID); // Void removing text input
												// event

			// Set/reset the inputbox contents
			clipInputEle.value = textToCopy ? textToCopy : "";

			// Get the scrollbar state and set the clipboard capturer position
			// in the viewport
			// to avoid scrolling the document
			var scrollPos = _getDocumentScrollPos();

			// Position the float (container) at the top left of the viewport,
			// but if the scroll bars are at zero, then place the float
			// outside of the document... this will completely conceal the float
			clipContainer.style.left = (scrollPos.left == 0 ? -50
					: scrollPos.left + 10)
					+ "px";
			clipContainer.style.top = (scrollPos.top == 0 ? -50
					: scrollPos.top + 10)
					+ "px";

			// Reveal the container
			clipContainer.style.display = "";

			// Select the "revealed" input box
			try {

				clipInputEle.focus();
				clipInputEle.select();

			} catch (e) {
			} // Mozilla sometimes throws XPConnect security exceptions

			var timeOutFunc = textToCopy ? afterNativeCopyClipInput
					: afterNativePasteClipInput;

			// Queue input-box removal function directly after native copy/paste
			// executes
			clipboardTOID = setTimeout(timeOutFunc, 0);

		}

		/**
		 * Safely hides the "temporary" clipboard input control
		 */
		function hideClipInput() {
			clipContainer.style.display = "none";
		}

		/**
		 * Invoked after the browser natively copies the "temporary" clipboard
		 * input control's content
		 */
		function afterNativeCopyClipInput() {
			clipboardTOID = null;
			hideClipInput();
			isSysClip = true;
			window.focus();
		}

		/**
		 * Invoked after the browser natively pastes the system clipboard text
		 * to the "temporary" clipboard input control.
		 */
		function afterNativePasteClipInput() {
			clipboardTOID = null;
			hideClipInput();

			// Ensure the cursor did not change/clear
			if (restoreCursor) {
				var curCursor = de.cursor.getCurrentCursorDesc();
				if (!curCursor || curCursor.domNode != restoreCursor.domNode
						|| curCursor.relIndex != restoreCursor.relIndex)
					de.cursor.setCursor(restoreCursor);
			}

			internalPaste(clipInputEle.value);

			window.focus();
		}

	})();
	// End Clibpboard.js
	// Start ContainerNormalization.js
	/*
	 * TODO: MAKE HTML VALID BY PLACING INLINE ANCESTORS WITHIN THE BLOCK-LEVEL
	 * CONTAINERS Inlines cannot contain blocks
	 * 
	 * 
	 * ARGGG... OK JUST RUBBISH THIS.. For all invalid inlines, remove them from
	 * the document.. that will naturally "normalize" the range. Cannot just
	 * move inlines into containers because containers may contain block
	 * decendants.
	 * 
	 * This will simplify the code a lot and keep the HTML tidy.
	 * 
	 * Should design algrorithms to assume valid HTML, ad if invalid then loss
	 * of formatting as a result of throwing away invalid tags doesnt matter.
	 */

	/**
	 * Creates containers, adjusts structure within the given range, such that
	 * it garauntees that all block-level elements in the range do not share any
	 * ancestor inline elements that occur between the containers and the
	 * common-block-level-ancestor, with other block-level elements. This
	 * property is useful for itemizing container in a given range. <br>
	 * This operation is undoable, and is intended for indentation and itemizing
	 * ranges.
	 * 
	 * @param {Node}
	 *            startNode The starting dom node of the range to normalize.
	 * 
	 * @param {Node}
	 *            endNode The ending dom node of the range to normalize. Can be
	 *            the same as start node or it must occur after the start dom
	 *            (in-order left-to-right traversal)
	 * 
	 * @param {Node}
	 *            containerTemplate (Optional) An element which supports inline
	 *            elements to be used for inline-groups which need containers.
	 *            Defaults to paragraph.
	 * 
	 * @return {[Node]} An array containing a list of all the top-level
	 *         containers in the given range, in order if traversing the dom
	 *         tree in-order. Can be empty.
	 * 
	 */
	function _getNormalizedContainerRange(startNode, endNode, containerTemplate) {

		var template = containerTemplate || $createElement("p");

		// Determine the master container for the normalization range
		var masterContainer = _findAncestor(_getCommonAncestor(startNode,
				endNode, true), docBody, _isBlockLevel, true)
				|| docBody;

		// Deepen start/end range
		while (startNode.firstChild) {
			startNode = startNode.firstChild;
		}
		while (endNode.lastChild) {
			endNode = endNode.lastChild;
		}

		// Check if the master container allows the container template
		if (masterContainer != docBody
				&& !_isValidRelationship(template, masterContainer)) {

			// Special case - list items
			if (_nodeName(masterContainer) == "ul"
					|| _nodeName(masterContainer) == "ol") {
				startNode = _findAncestor(startNode, masterContainer);
				endNode = _findAncestor(endNode, masterContainer);
				var containers = [];
				while (startNode) {
					if (_nodeName(startNode) == "li")
						containers.push(startNode);
					startNode = startNode == endNode ? null
							: startNode.nextSibling;
				}
				return containers;

			}

			// If the master container doesn't allow sub-containers, then the
			// normalized range
			// becomes a single container... the master container.
			return [ masterContainer ]
		}

		// Extend range to point to begin and start at top-level sub-containers
		// if they exist,
		// or to the end of inline groups
		var extendedRangeNode, protectedContainers = [], reinsertProcContainers = [], extendedStart, extendedEnd;

		// Extend start range
		_visitAllNodes(masterContainer, startNode, false, extendRange);

		// Remove any protect containers
		if (protectedContainers.length > 0) {
			for ( var i in protectedContainers) {
				if (protectedContainers[i].parentNode) {
					protectedContainers[i].parentNode
							.removeChild(protectedContainers[i]);
					reinsertProcContainers.push(protectedContainers[i]);
				}
			}

		}

		extendedStart = extendedRangeNode;
		if (!extendedStart) {
			// Set start through to end of initial inline group
			extendedStart = masterContainer.firstChild;
			while (extendedStart.firstChild) {
				extendedStart = extendedStart.firstChild;
			}
		}

		// Extend end range
		extendedRangeNode = null;
		protectedContainers = [];
		_visitAllNodes(masterContainer, endNode, true, extendRange);

		// Remove any protect containers
		if (protectedContainers.length > 0) {
			for ( var i in protectedContainers) {
				if (protectedContainers[i].parentNode) {
					protectedContainers[i].parentNode
							.removeChild(protectedContainers[i]);
					reinsertProcContainers.push(protectedContainers[i]);
				}
			}
		}

		extendedEnd = extendedRangeNode;
		if (!extendedEnd) {
			// Set start through to end of post inline group
			extendedEnd = masterContainer.lastChild;
			while (extendedEnd.lastChild) {
				extendedEnd = extendedEnd.lastChild;
			}
		}

		// Move protected nodes out of the extended range if there are any
		for ( var i in reinsertProcContainers) {
			docBody.appendChild(reinsertProcContainers[i]);
		}

		// The range only containers protected nodes
		if (!extendedEnd || !extendedStart)
			return [];

		// Perform normalization operations (two phases)
		separateBlockPaths(extendedStart, extendedEnd, masterContainer);
		encapsulateIGroups(extendedStart, extendedEnd, masterContainer,
				template);

		// Get top-level containers within the original range
		var tdc = _findAncestor(startNode, masterContainer);
		endNode = _findAncestor(endNode, masterContainer);

		var range = [];

		do {
			var node = tdc;
			while (node) {
				if (_isBlockLevel(node)) {
					range.push(node);
					break;
				}
				node = node.firstChild;
			}
			tdc = tdc == endNode ? null : tdc.nextSibling;
		} while (tdc);

		return range;

		/**
		 * Helper function for extending the range backward/forward. Sets the
		 * extendedRangeNode local iff a top-level block container is found
		 * within the master container
		 * 
		 * @param {Node}
		 *            domNode Provided by de.visit function
		 */
		function extendRange(domNode) {
			if (domNode == masterContainer)
				return;

			// Find top-level block element from the master-container -
			// inclusive of self
			var node = domNode, blNode = null;
			while (node != masterContainer) {
				if (_isBlockLevel(node))
					blNode = node;

				// If this sub-tree is protected then note the protected node
				// root
				if (node.parentNode == masterContainer
						&& de.doc.getProtectedNodeContainer(node) == node) {
					protectedContainers.push(node);
					blNode = null; // Ignore this since this subtree will be
									// repositioned in the document
				}

				node = node.parentNode;
			}

			if (blNode) {
				// If this node has a top-level block element from the
				// master-container
				// then the extended start has been located
				extendedRangeNode = blNode;
				return false;
			}
		}

		/**
		 * Ensures that all first occuring block-level elements from the master
		 * container own their own path up to the master container.
		 * 
		 * @param {Node}
		 *            startNode The start node in the range. This must not have
		 *            a block-level element in the path from itself up to the
		 *            master container (exclusive).
		 * 
		 * @param {Node}
		 *            endNode The end node in the range.
		 * 
		 * @param {Node}
		 *            masterContainer The master container for all sub
		 *            containers
		 * 
		 */
		function separateBlockPaths(startNode, endNode, masterContainer) {

			var scanPoint = startNode, seenEndPoint = false;

			// Scan through range for all top-level block elements from the
			// master container
			while (scanPoint && !seenEndPoint) {

				_visitAllNodes(
						masterContainer,
						scanPoint,
						true,
						function(domNode) {

							scanPoint = null;
							seenEndPoint |= domNode == endNode;

							if (domNode == masterContainer)
								return;

							// If this dom node is a block element then ensure
							// that it owns its own path
							// up to the master container.
							if (_isBlockLevel(domNode)) {

								// Note: this block level element is top-level
								// from the master container... i.e. there is no
								// block-level elements between this node up to
								// the master container

								var pivotNode = domNode;
								while (pivotNode.parentNode != masterContainer) {

									// Separate previous siblings into duplicate
									// inline node
									if (pivotNode.previousSibling) {

										// Clone shared inline element
										var clone = pivotNode.parentNode
												.cloneNode(false);

										// Migrate previous siblings into duped
										// inline element
										while (pivotNode.previousSibling) {

											var psib = pivotNode.previousSibling;

											_execOp(_Operation.REMOVE_NODE,
													psib);
											_execOp(_Operation.INSERT_NODE,
													psib, clone, 0);

										}

										// Insert the cloned inline element back
										// into the document
										_execOp(
												_Operation.INSERT_NODE,
												clone,
												pivotNode.parentNode.parentNode,
												_indexInParent(pivotNode.parentNode));

									}

									// Separate next siblings into duplicate
									// inline node
									if (pivotNode.nextSibling) {

										// Clone shared inline element
										var clone = pivotNode.parentNode
												.cloneNode(false)

										// Migrate next siblings into duped
										// inline element
										while (pivotNode.nextSibling) {
											var nsib = pivotNode.nextSibling;
											_execOp(_Operation.REMOVE_NODE,
													nsib);
											_execOp(_Operation.INSERT_NODE,
													nsib, clone);
										}

										// Insert the cloned inline element back
										// into the document
										_execOp(
												_Operation.INSERT_NODE,
												clone,
												pivotNode.parentNode.parentNode,
												_indexInParent(pivotNode.parentNode) + 1);

									}

									pivotNode = pivotNode.parentNode;

								} // End separating path to master container

								// This dom node now owns its own path to the
								// master container.
								// Setup to scan down next path
								scanPoint = pivotNode.nextSibling; // NB: Pivot
																	// is an
																	// immediate
																	// child of
																	// master
																	// container

								// Check if the end node lies within this
								// block-level element
								_visitAllNodes(domNode, domNode, true,
										function(subDomNode) {
											if (subDomNode == endNode)
												seenEndPoint = true;
											return !seenEndPoint;
										});

								return false;
							}

							return !seenEndPoint;

						});

			} // End Loop: separating paths to block-level elements in given
				// range

		} // End function separateBlockPaths

		/**
		 * For all sub-trees within the given range of the master container
		 * (i.e. Immediate children of the master continer), all sub-trees
		 * containing only inline elements/text nodes are migrated into a
		 * containerTemplate clone within the master container.
		 * 
		 * 
		 * @param {Node}
		 *            startNode The start node to encapsulate from. This should
		 *            not be within an inline sub-tree which has a previous
		 *            sub-tree which contains only inline elements.
		 * 
		 * @param {Node}
		 *            endNode The end node to encapsulate to. This should not be
		 *            within an inline sub-tree which has a following sub-tree
		 *            which contains only inline elements.
		 * 
		 * @param {Node}
		 *            masterContainer The master container
		 * 
		 * @param {Node}
		 *            containerTemplate The container to clone for migrating
		 *            inline groups into
		 * 
		 */
		function encapsulateIGroups(startNode, endNode, masterContainer,
				containerTemplate) {

			// Adjust start and end nodes to their root nodes within the master
			// container
			startNode = _findAncestor(startNode, masterContainer);
			endNode = _findAncestor(endNode, masterContainer);

			var subTreeNode = startNode, inlineGroupStart = null;

			// Visit all sub-trees in the master container within the range
			// (i.e. immediate children in the master contianer).
			while (subTreeNode) {

				var domNode = subTreeNode, containsBlock = false;

				while (domNode) {
					if (_isBlockLevel(domNode)) {
						containsBlock = true;
						break;
					}
					// Just check down left-most-path since all block level
					// elements in
					// the given range have their own path to the master
					// container
					domNode = domNode.firstChild;
				}

				if (containsBlock) {

					if (inlineGroupStart) {
						encapsulate(inlineGroupStart, subTreeNode);
						inlineGroupStart = null;
					}

				} else {
					// Mark start of inline group - avoid including whitespace
					// nodes which should not be encapsulated.
					// For example, whitespace in between list items
					if (!inlineGroupStart) {
						if (!(subTreeNode.nodeType == Node.TEXT_NODE && !_doesTextSupportNonWS(subTreeNode)))
							inlineGroupStart = subTreeNode;
					}
				}

				subTreeNode = subTreeNode == endNode ? null
						: subTreeNode.nextSibling;
			}

			if (inlineGroupStart)
				encapsulate(inlineGroupStart, null);

			/**
			 * Encapsulates a run of inline siblings within the master
			 * container.
			 * 
			 * @param {Node}
			 *            start The sibling to start encapsulating from.
			 * 
			 * @param {Node}
			 *            endEx The exclusive end sibling. Can be null for
			 *            encapsulating all siblings from start node.
			 */
			function encapsulate(start, endEx) {

				var igContainer = containerTemplate.cloneNode(false), igSubTree = start;

				// Insert the new inline-group container into the master
				// container
				debug
						.assert(_isValidRelationship(igContainer,
								masterContainer));
				_execOp(_Operation.INSERT_NODE, igContainer, masterContainer,
						_indexInParent(start));

				// Migrate inline run into the new container
				while (igSubTree != endEx) {
					var nextSib = igSubTree.nextSibling;
					_execOp(_Operation.REMOVE_NODE, igSubTree);
					_execOp(_Operation.INSERT_NODE, igSubTree, igContainer);
					igSubTree = nextSib;
				}
			}

		} // End function encapsulateIGroups

	} // End getNormalizedContainerRange
	// End ContainerNormalization.js
	// StarCursor.js
	(function() {

		// TODO: REFACTOR de.cursor ro de.Cursor ... do when refactor de prefix
		// to chosen name

		/*
		 * A cursor descriptor that represents the current cursor that is
		 * showing. Null if no cursor showing
		 */
		var currentCursorDesc,

		/* The visual representation for the cursor on the actual web page */
		cursorDiv,

		/* A Constant defining the time in ms between cursor blinks */
		CURSOR_BLINK_MS_TIME = 460,

		/* The cursor blinker timeout ID */
		cursorBlinkTOId,

		/*
		 * The x position which the cursor should realign to when moving up/down
		 * the content
		 */
		cursorXAlign,

		/* Used for measurement purposes */
		measureSpanEl, measureSpanTextNode, measurePreTextNode, measurePostTextNode, measureFullText,

		// ------------- Define lookup maps according to the DirectEdit
		// DOM-based Web Editor Specification 1.0 -------------

		/*
		 * Refer to specification 2.2.2 Elements which the cursor can appear
		 * directly before
		 */
		beforeElements = $createLookupMap("img,table,input,select,button,textarea,object"),

		/*
		 * Refer to specification 2.2.3 Elements which the cursor can appear
		 * directly after
		 */
		afterElements = $createLookupMap("img,table,input,select,button,textarea,object,br");

		$enqueueInit("Cursor", function() {

			// Create elements for measurement purposes
			measureSpanEl = $createElement("span");
			measureSpanTextNode = document.createTextNode("");
			measureSpanEl.appendChild(measureSpanTextNode);
			measurePreTextNode = document.createTextNode("");

			// Create cursor div and add it to the document
			cursorDiv = $createElement("div");
			_setClassName(cursorDiv, _PROTECTED_CLASS + " sw-cursor"); // Avoid
																		// the
																		// cursor
																		// from
																		// being
																		// edited
			docBody.appendChild(cursorDiv);

			// Set cursor background and z-index to defaults if css sheets don't
			// supply them
			var cursorStyle = "", cssVal = _getComputedStyle(cursorDiv,
					'z-index');

			if (!cssVal || cssVal == "0" || cssVal == "auto")
				;
			cursorStyle += "z-index:100";

			_setFullStyle(cursorDiv,
					"position:absolute; width:2px;visibility:hidden;"
							+ cursorStyle);

			// Register to events
			_addHandler(window, "resize", onWindowResized);
			_addHandler(document, "keystroke", onKeyStroke);

			// Make as subject
			_model(de.cursor);

			// Keep cursor in view after actions are executed
			de.UndoMan.addObserver({
				onAfterExec : scrollToCursor,
				onAfterUndo : scrollToCursor,
				onAfterRedo : scrollToCursor
			});

		}, "UndoMan");

		/**
		 * @namespace The cursor namespace packages cursor specific operations.
		 */
		de.cursor = {

			/**
			 * @class Provides flag constants for describing the cursor relation
			 *        to the dom node.
			 */
			PlacementFlag : {

				/**
				 * Read Only: Refer to specification 2.2.1. AKA Text nodes
				 * (containing least one renderable symbol)
				 * 
				 * @type Number
				 */
				INSIDE : 1, // @REPLACE de.cursor.PlacementFlag.INSIDE 1

				/**
				 * Read Only: Refer to specification 2.2.2
				 * 
				 * @type Number
				 */
				BEFORE : 2, // @REPLACE de.cursor.PlacementFlag.BEFORE 2

				/**
				 * Read Only: Refer to specification 2.2.3
				 * 
				 * @type Number
				 */
				AFTER : 4
			// @REPLACE de.cursor.PlacementFlag.AFTER 4
			},

			/**
			 * @param {Node}
			 *            domNode a dom node to test
			 * @return {Boolean} True iff the dom node can support a cursor
			 *         placed by it.
			 */
			doesNodeSupportCursor : function(domNode) {
				return !de.doc.isProtectedNode(domNode)
						&& de.doc.isNodeEditable(domNode);
			},

			/**
			 * Sets the new cursor. The position is updated immediatly. Set to
			 * null to hide/destroy the cursor.
			 * 
			 * If the cursor is not in an editable area, then it will be set to
			 * null
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            cursorDesc The new cursor. Null for no cursor.
			 * 
			 * @return {Boolean} True if there is a cursor after the operation.
			 *         False if the operation resulted in no cursor.
			 */
			setCursor : function(cursorDesc) {

				// Dissallow cursor placement at protected nodes
				if (cursorDesc
						&& !this.doesNodeSupportCursor(cursorDesc.domNode))
					cursorDesc = null;

				// Set the new cursor info
				currentCursorDesc = cursorDesc;

				// Stop and hide the cursor blink
				cursorBlink(false);

				// Update cursor GUI
				if (currentCursorDesc) {

					// Update cursor position
					cursorDiv.style.left = (currentCursorDesc.docLeft + (currentCursorDesc.isRightOf ? currentCursorDesc.width
							: 0))
							+ "px";
					cursorDiv.style.top = currentCursorDesc.docTop + "px";
					cursorDiv.style.height = currentCursorDesc.height + "px";

					// Determine color for the cursor.
					var color = _getComputedStyle(currentCursorDesc.domNode,
							"color");
					cursorDiv.style.backgroundColor = color ? color : "black";

					// Begin cursor blink
					cursorBlink(true);
				}

				// Always reset cursorXAlign
				cursorXAlign = null;

				// Notify observers of cursor change
				this.fireEvent("CursorChanged", this.getCurrentCursorDesc());

				return currentCursorDesc != null;

			},

			/**
			 * Discovers the closest matching cursor descriptor for a given
			 * position.
			 * 
			 * @param {Number}
			 *            targetX The X coordinate in the window from where to
			 *            find the closest cursor position.
			 * 
			 * @param {Number}
			 *            targetY The Y coordinate in the window from where to
			 *            find the closest cursor position.
			 * 
			 * @param {Node}
			 *            targetNode (optional) The node at the given position.
			 *            If not provided this will be determined for you.
			 * 
			 * @return {de.cursor.CursorDescriptor} A CursorInfo object
			 *         containing the closest cursor position to the given
			 *         target coordinates. Null if there are no valid nearby
			 *         cursor positions.
			 */
			getCursorDescAtXY : getCursorDescAtXY,

			/**
			 * @return {de.cursor.CursorDescriptor} The clone of the current
			 *         cursor info object. Null if there is none.
			 */
			getCurrentCursorDesc : function() {
				return currentCursorDesc ? _clone(currentCursorDesc) : null;
			},

			/**
			 * Updates the cursor GUI.
			 */
			refreshCursor : function() {
				if (!currentCursorDesc)
					return;

				// IE Has this annoying event-threading model where when
				// querrying some
				// dom objects' properties IE instantly raises an event on the
				// same trace of execution!
				// This causes a nasty bug when using any of the cursor
				// alrgorithms, this function
				// is 'randomly' invoked during critical sections which depend
				// on the measuring nodes -
				// since a resize event is invoked due to querrying various
				// spatial information on dom objects,
				// such as getting the document.scrollLeft for rereiving a
				// elements absolute position in the window.
				// Work around:
				if (measurePostTextNode)
					return;

				// Get new position
				setSpatialMembers(currentCursorDesc);

				// Update the gui
				cursorDiv.style.left = (currentCursorDesc.docLeft + (currentCursorDesc.isRightOf ? currentCursorDesc.width
						: 0))
						+ "px";
				cursorDiv.style.top = currentCursorDesc.docTop + "px";
				cursorDiv.style.height = currentCursorDesc.height + "px";

			},

			/**
			 * Scrolls the document to view the current cursor
			 */
			scrollToCursor : scrollToCursor,

			/**
			 * @return {Boolean} True if the cursor is shown. False if no cursor
			 *         exists.
			 */
			exists : function() {
				return currentCursorDesc != null;
			},

			/**
			 * @param {Node}
			 *            node A DOM node to test
			 * @return {Boolean} True if node is the cursor blinker element.
			 */
			isCursorEle : function(node) {
				return node == cursorDiv;
			},

			/**
			 * A wrapper function for _getRenderedNodeAtXY - excludes
			 * 
			 * @param {Number}
			 *            x The x pixel coordinate relative to the window.
			 * @param {Number}
			 *            y The x pixel coordinate relative to the window.
			 * @return {Node} The node at the given window position - gauranteed
			 *         not to be the cursor blinker.
			 */
			getNonCursorNodeAtXY : function(x, y) {
				var dval = cursorDiv.style.display;
				cursorDiv.style.display = "none"; // Ensure not visible
				var targetNode = _getRenderedNodeAtXY(x, y);
				cursorDiv.style.display = dval; // Restore original value
				return targetNode;
			},

			/**
			 * Gets the next cursor descriptor before or after a given cursor
			 * descriptor. I.E. The next physical move of the cursor.
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            cursorDesc The cursor descriptor to reference from. it
			 *            does not have to be a valid cursor descriptor, that
			 *            is, a node which the cursor cannot be place in/next
			 *            to.
			 * 
			 * @param {Boolean}
			 *            left True to get cursor desc left of reference point,
			 *            false for right.
			 * 
			 * @return {de.cursor.CursorDescriptor} The next cursor move from
			 *         the reference point. Null if there is none. Note that it
			 *         might be outside of an editable section.
			 * 
			 */
			getNextCursorMovement : getNextCursorMovement,

			/**
			 * Note: only uses y and height members
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            desc1 A cursor to compare
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            desc2 A cursor to compare
			 * 
			 * @return {Boolean} True iff desc1 and desc2 are on the same line.
			 */
			isOnSameLine : isOnSameLine,

			/**
			 * Builds a cursor descriptor (provides the spatial information).
			 * 
			 * @param {Node}
			 *            domNode The dom node
			 * 
			 * @param {Number}
			 *            relIndex The index within the dom node (only applies
			 *            for text nodes)
			 * 
			 * @param {Boolean}
			 *            isRightOf True if the cursor is to the right of index
			 *            position.
			 * 
			 * @return {de.cursor.CursorDescriptor} The cursor descriptor. Null
			 *         If a cursor cannot be placed at the given position. Note:
			 *         if the dom not is a text node but the index is not
			 *         rendered then null will be returned.
			 * 
			 */
			createCursorDesc : function(domNode, relIndex, isRightOf) {

				var placement = getPlacementFlags(domNode);

				if (placement == 0
						|| (placement != de.cursor.PlacementFlag.INSIDE && ((isRightOf && !(placement & de.cursor.PlacementFlag.AFTER)) || (!isRightOf && !(placement & de.cursor.PlacementFlag.BEFORE)))))
					return null;

				var desc = {
					domNode : domNode,
					relIndex : relIndex,
					isRightOf : isRightOf,
					placement : placement
				};

				setSpatialMembers(desc);

				// If the text node / index is not rendered then return null
				if (placement == de.cursor.PlacementFlag.INSIDE
						&& (desc.width == 0 || desc.height == 0))
					return null;

				return desc;

			},

			/**
			 * Note: even if de.cursor.PlacementFlag.INSIDE is returned, the
			 * text node may not be able to support a cursor being placed inside
			 * if it contains nothing but non-renderable symbols.
			 * 
			 * @param {Node}
			 *            node A dom node to get the placement flags for
			 * 
			 * @return {Number} A bitwise or combination of
			 *         de.cursor.PlacementFlag's. Zero if it is not a candidate
			 *         for supporting a cursor placement.
			 */
			getPlacementFlags : getPlacementFlags,

			/**
			 * Gets the nearest cursor descriptor to the given position
			 * information.
			 * 
			 * @param {Node}
			 *            domNode A dom node in the document
			 * 
			 * @param {Number}
			 *            relIndex A relative index in the dom node
			 * 
			 * @param {Boolean}
			 *            isRightOf True if the cursor is placed to the right of
			 *            the node/index. False for left placement.
			 * 
			 * @param {Boolean}
			 *            searchLeft True to search for nearest cursor to the
			 *            left of the given position. False to search right.
			 * 
			 * @return {de.cursor.CursorDescriptor} A cursor descriptor nearest
			 *         to the given position. Null if there is none.
			 */
			getNearestCursorDesc : function(domNode, relIndex, isRightOf,
					searchLeft) {

				if (_nodeName(domNode) == "br")
					isRightOf = true;

				var cDesc = de.cursor.createCursorDesc(domNode, relIndex,
						isRightOf); // Gives null if invalid request

				// Special case: line breaks
				if (cDesc && _nodeName(domNode) == "br") {

					// Get the cursor pos to the left and right of this line
					// break
					var leftCDesc = getNextCursorMovement(cDesc, true), rightCDesc = getNextCursorMovement(
							cDesc, false);

					// If this line break is on its own line then return it
					if ((!leftCDesc || !isOnSameLine(leftCDesc, cDesc))
							&& (!rightCDesc || !isOnSameLine(rightCDesc, cDesc)))
						return cDesc;

					// Otherwise return the cursor pos to the left/right of the
					// break - if it exists
					return (searchLeft ? leftCDesc : rightCDesc) || cDesc;
				}

				return cDesc || getNextCursorMovement({
					domNode : domNode,
					isRightOf : isRightOf,
					relIndex : relIndex,
					y : _getPositionInWindow(domNode).y
				}, searchLeft);

			} // End getNearestCursorDesc function

		}; // End Cursor Namespace

		/* -------------------------------------------------------------------------------------- */
		// Events
		/* -------------------------------------------------------------------------------------- */

		function onKeyStroke(e, normalizedKey) {

			if (!currentCursorDesc || e.ctrlKey || e.metaKey || e.altKey)
				return;

			var moveAction = 0;

			switch (normalizedKey) {
			case "Left": // Arrow left
			case "Right": // Arrow right

				moveAction = 1;

				// Attempt to move the cursor left/right
				var neighbour = getNextCursorMovement(currentCursorDesc,
						normalizedKey == "Left");

				// If the neighbour is null, then reached end point
				if (!neighbour)
					neighbour = currentCursorDesc;

				var oldCDesc = currentCursorDesc;

				if (!de.cursor.setCursor(neighbour) && oldCDesc)
					// If failed to set the cursor (probably due to it being
					// outside
					// of editable section) then set back to the old cursor
					// position
					de.cursor.setCursor(oldCDesc)

				break;

			case "Up": // Arrow up
			case "Down": // Arrow down

				moveAction = 1;

				var isUpward = normalizedKey == "Up";

				var docScrollPos = _getDocumentScrollPos();

				var xAlign = cursorXAlign ? cursorXAlign
						: (currentCursorDesc.docLeft - docScrollPos.left)
								+ (currentCursorDesc.isRightOf ? currentCursorDesc.width
										: 0);

				var searchSpace = getUpDownSearchSpace();
				var cDesc;

				if (searchSpace) {
					// Perform specialized/narrowed dual binary search to
					// discover cursor position directly above/below
					cDesc = searchBestCursorPos(xAlign,
							currentCursorDesc.docTop - docScrollPos.top,
							searchSpace, currentCursorDesc, isUpward);
					restoreMeasuringNodes();
				}

				if (!cDesc)
					cDesc = currentCursorDesc;

				var oldCDesc = currentCursorDesc;

				if (!de.cursor.setCursor(cDesc) && oldCDesc)
					// If failed to set the cursor (probably due to it being
					// outside
					// of editable section) then set back to the old cursor
					// position
					de.cursor.setCursor(oldCDesc)

					// Remember for next up/down movement
				cursorXAlign = xAlign;

				break;

			} // End case

			if (moveAction) {
				scrollToCursor(); // Auto scroll
				return false; // Consume key event
			}

			return true;

			// Inner support functions to follow

			/**
			 * An inner supporting function.
			 * 
			 * @return {Object} The search space for a dual binary search
			 */
			function getUpDownSearchSpace() {

				// Build array of all nodes to search inside of targetEl
				var nodesToSearch = [];
				var pendingBANodes = [];
				var nextLine;

				_visitAllNodes(
						docBody,
						currentCursorDesc.domNode,
						!isUpward,
						function(domNode) {

							// Add in any pending before/after nodes
							appendPendingBAnodes(domNode);

							var placementFlags = getPlacementFlags(domNode);

							if (placementFlags == 0)
								return true; // Cursor cannot be placed in
												// this node

							// Check to see if found a new line
							var pos, height;
							if (domNode.nodeType == Node.TEXT_NODE) {
								pos = _getPositionInWindow(domNode.parentNode);
								height = domNode.parentNode.offsetHeight;
							} else if (_nodeName(domNode) == "br") {
								pos = measureLineBreak(domNode)
								height = pos.height;
							} else {
								pos = _getPositionInWindow(domNode);
								height = domNode.offsetHeight;
							}

							if (!isOnSameLine(nextLine ? nextLine
									: currentCursorDesc, {
								y : pos.y,
								height : height
							})) {
								// If already found the next line then the
								// searchspace can end here
								if (nextLine)
									return false;

								// If this is the first node that is in the next
								// line, then remeber line information
								nextLine = {
									y : pos.y,
									height : height
								};
							}

							if (placementFlags == de.cursor.PlacementFlag.INSIDE) { // AKA
																					// a
																					// text
																					// node
								nodesToSearch.push({
									domNode : domNode,
									placement : de.cursor.PlacementFlag.INSIDE
								});

							} else { // Before and/or After

								if (placementFlags
										& de.cursor.PlacementFlag.BEFORE) {
									if (isUpward) {
										pendingBANodes.push(domNode);
									} else {
										nodesToSearch
												.push({
													domNode : domNode,
													placement : de.cursor.PlacementFlag.BEFORE
												// Only store before flag
												});
									}
								}

								if (placementFlags
										& de.cursor.PlacementFlag.AFTER) {
									if (isUpward) {
										nodesToSearch
												.push({
													domNode : domNode,
													placement : de.cursor.PlacementFlag.AFTER
												// Only store before flag
												});
									} else {
										pendingBANodes.push(domNode);
									}
								}

							}

						});

				// Was the next line even found?
				if (!nextLine)
					return null;

				// Append any pending before/after nodes
				appendPendingBAnodes(null);

				// Make sure search space is top-down
				if (isUpward)
					nodesToSearch.reverse();

				// Suppliment search space with index information
				var totalPlacementLength = 0;
				for ( var i in nodesToSearch) {
					var node = nodesToSearch[i];
					var len = _nodeLength(node.domNode, 1);
					node.startIndex = totalPlacementLength;
					node.endIndex = node.startIndex + (len - 1);
					node.length = len;
					totalPlacementLength += len;
				}

				return {
					nodes : nodesToSearch,
					totalLength : totalPlacementLength
				};

				/**
				 * Adds pending before/after nodes to the nodesToSearch
				 * 
				 * @param {Node}
				 *            domNode the current dom node or null
				 */
				function appendPendingBAnodes(domNode) {

					while (pendingBANodes.length > 0) {

						if (!domNode
								|| !_isAncestor(
										pendingBANodes[pendingBANodes.length - 1],
										domNode)) {
							nodesToSearch
									.push({
										domNode : pendingBANodes.pop(),
										placement : isUpward ? de.cursor.PlacementFlag.BEFORE
												: de.cursor.PlacementFlag.AFTER
									});

						} else
							break;
					}

				} // End inner appendPendingBAnodes

			} // End inner getUpDownSearchSpace

		} // End onKeyStroke

		/**
		 * If the window resized, the cursor must be re-positioned.
		 * 
		 * @param {Event}
		 *            e
		 */
		function onWindowResized(e) {
			de.cursor.refreshCursor();
		}

		/* -------------------------------------------------------------------------------------- */
		// Support functions for cursor GUI
		/* -------------------------------------------------------------------------------------- */

		/**
		 * @param {Boolean}
		 *            visible True to toggle cursor div to visible, false to
		 *            hidden.
		 */
		function setCursorVisible(visible) {
			cursorDiv.style.visibility = visible ? "visible" : "hidden";
		}
		/**
		 * @return {Boolean} True if the cursor div is visisble.
		 */
		function isCursorVisible() {
			return cursorDiv.style.visibility == "visible";
		}

		/**
		 * Continuously toggles the cursor div's visibility over time
		 * 
		 * @param {Boolean}
		 *            on True to turn it on, false to turn it off and hide it.
		 */
		function cursorBlink(on) {

			if (typeof on != "undefined")
				cursorBlink.on = on;

			if (cursorBlink.on) {
				setCursorVisible(!isCursorVisible());
				cursorBlinkTOId = setTimeout(cursorBlink, CURSOR_BLINK_MS_TIME,
						1); // NB: IE does not pass arguments
			} else if (cursorBlinkTOId) {
				clearTimeout(cursorBlinkTOId);
				cursorBlinkTOId = null;
				setCursorVisible(false);
			}
		}

		/**
		 * Scrolls the document to the current cursor's position
		 */
		function scrollToCursor() {

			if (!currentCursorDesc)
				return;

			var viewPortSize = _getViewPortSize(), dx = 0, dy = 0;

			// Get dy
			if ((currentCursorDesc.y + currentCursorDesc.height) >= viewPortSize.height) {
				dy = (currentCursorDesc.y + currentCursorDesc.height)
						- viewPortSize.height;
			} else if (currentCursorDesc.y < 0) {
				dy = currentCursorDesc.y;
			}

			// get dx
			var xpos = currentCursorDesc.x
					+ (currentCursorDesc.isRightOf ? currentCursorDesc.width
							: 0) + parseInt(cursorDiv.style.width);
			if (xpos >= viewPortSize.width) {
				dx = xpos - viewPortSize.width;
			} else if (currentCursorDesc.x < 0) {
				dx = currentCursorDesc.x;
			}

			if (dx || dy)
				window.scrollBy(dx, dy);

		}

		/* -------------------------------------------------------------------------------------- */
		// Support functions for locating cursor information
		/* -------------------------------------------------------------------------------------- */

		// See de.cursor.getPlacementFlags doc
		function getPlacementFlags(node) {

			// Always place cursor after the packages.
			// NOTE: Testing this first to avoid placing cursor in placeholders
			// within the packaged nodes.
			var pcon = de.doc.getPackageContainer(node);
			if (pcon) {
				var pflags = 0;
				if (pcon == node)
					pflags = de.cursor.PlacementFlag.BEFORE
							| de.cursor.PlacementFlag.AFTER;
				return pflags;
			}

			// Cursors can be placed before placeholders, but not inside of them
			if (de.doc.isESPlaceHolder(node, false))
				return (de.doc.isESPlaceHolder(node, true)) ? de.cursor.PlacementFlag.BEFORE
						: 0;

			if (de.doc.isMNPlaceHolder(node, false))
				return (de.doc.isMNPlaceHolder(node, true)) ? de.cursor.PlacementFlag.BEFORE
						: 0;

			if (node.nodeType == Node.TEXT_NODE) {

				if (_doesTextSupportNonWS(node))
					return de.cursor.PlacementFlag.INSIDE;

				return 0;
			}

			var flags = 0;

			// TODO: REFACTOR/DOCUMENT
			// Apply user flag funciton if specified
			if (de.cursor.usrGetPlacementFlags) {
				flags = de.cursor.usrGetPlacementFlags(node);
				if (flags === $undefined)
					flags = 0;
				else
					return flags;
			}

			if (beforeElements[_nodeName(node)])
				flags = de.cursor.PlacementFlag.BEFORE;

			if (afterElements[_nodeName(node)])
				flags |= de.cursor.PlacementFlag.AFTER;

			return flags;
		}

		/**
		 * Determines whether cinf1 is closer to target than cinf2. Uses adx/ady
		 * information
		 * 
		 * @param {de.cursor.CursorDescriptor}
		 *            desc1 A cursor to compare
		 * @param {de.cursor.CursorDescriptor}
		 *            desc2 A cursor to compare
		 * @param {Number}
		 *            targetY The target Y (used for boundry cases)
		 * 
		 * @return True if desc1 is closer than desc2.
		 */
		function isCloserToTarget(desc1, desc2, targetY) {

			// If they are on the same line...
			if (isOnSameLine(desc1, desc2)) {
				// Then compare there absolute delta x's
				return desc1.adx < desc2.adx;
			} else if (desc1.ady == desc2.ady) {
				// Else if not on the same line AND they have the same absolute
				// delta y's
				// then choose the closest to target Y from in the middle of
				// their height
				return Math.abs((desc1.y + (desc1.height / 2)) - targetY) < Math
						.abs((desc2.y + (desc2.height / 2)) - targetY);
			}

			// They do not accur on the same line, and have diffent ady's... so
			// pick closest to target Y
			return desc1.ady < desc2.ady;
		}

		/**
		 * Determines whether desc1 is closer to (x, y) than desc2.
		 * 
		 * @param {de.cursor.CursorDescriptor}
		 *            desc1 A cursor to compare
		 * @param {de.cursor.CursorDescriptor}
		 *            desc2 A cursor to compare
		 * @param {Number}
		 *            tx The target x-coord
		 * @param {Number}
		 *            ty The target y-coor
		 * 
		 * @return True if desc1 is closer than desc2.
		 */
		function isCloserToXY(desc1, desc2, x, y) {

			// If they are on the same line then compare there absolute delta
			// x's
			if (isOnSameLine(desc1, desc2))
				return Math.abs(desc1.x - x) < Math.abs(desc2.x - x);

			var ady1 = Math.min(Math.abs(desc1.y - y), Math
					.abs((desc1.y + desc1.height) - y));
			var ady2 = Math.min(Math.abs(desc2.y - y), Math
					.abs((desc2.y + desc2.height) - y));

			// Else if not on the same line AND they have the same absolute
			// delta y's
			// then choose the closest to target Y from in the middle of their
			// height
			if (ady1 == ady2)
				return Math.abs((desc1.y + (desc1.height / 2)) - y) < Math
						.abs((desc2.y + (desc2.height / 2)) - y);

			// They do not accur on the same line, and have diffent ady's... so
			// pick closest to target Y
			return ady1 < ady2;
		}

		// See de.cursor.isOnSameLine jsdoc
		function isOnSameLine(desc1, desc2) {
			return (desc1.y >= desc2.y && desc1.y < (desc2.y + desc2.height))
					|| (desc2.y >= desc1.y && desc2.y < (desc1.y + desc1.height));
		}

		/**
		 * If there are any elements/nodes in the document that were used for
		 * measurement purposes (via setupMeasuringNodes).
		 */
		function restoreMeasuringNodes() {
			if (measurePostTextNode) {
				measurePostTextNode.parentNode.removeChild(measureSpanEl);
				measurePostTextNode.parentNode.removeChild(measurePreTextNode);
				measurePostTextNode.nodeValue = measureFullText;
				measurePostTextNode = null;
			}
		}

		/**
		 * Lazily sets up measuring elements/nodes in document. Be sure to call
		 * restoreMeasuringNodes if you want the nodes to be removed (i.e. The
		 * DOM document to return to it's original state)
		 * 
		 * @param {Node}
		 *            textNode The text-node for which measurements are to take
		 *            place.
		 */
		function setupMeasuringNodes(textNode) {

			if (measurePostTextNode == textNode)
				return;
			if (measurePostTextNode)
				restoreMeasuringNodes();

			measurePostTextNode = textNode;
			measureFullText = textNode.nodeValue;

			// Split the text node into 3 nodes (including self)
			textNode.parentNode.insertBefore(measureSpanEl, textNode);
			textNode.parentNode.insertBefore(measurePreTextNode, measureSpanEl);
		}

		/**
		 * Requires that setupMeasuringNodes has been invoked. Isolates measure
		 * nodes so that the measureSpanEl encapsulates a single charactor at a
		 * given index.
		 * 
		 * @param {Number}
		 *            index The index of the charactor to isolate.
		 * @return True if the isolated charactor is a renderable symbol.
		 */
		function measureCharactor(index) {
			measurePreTextNode.nodeValue = measureFullText.substr(0, index);
			measureSpanTextNode.nodeValue = measureFullText.charAt(index);
			measurePostTextNode.nodeValue = measureFullText.substr(index + 1);
			return measureSpanEl.offsetHeight != 0
					&& measureSpanEl.offsetWidth != 0;
		}

		/**
		 * Measures a line break element. Restores any measuring nodes before
		 * and after operation.
		 * 
		 * @param {Node}
		 *            lb A line break element to measure.
		 * @return {Object} A tuple containing the position of the line break in
		 *         the window, and its height.
		 */
		function measureLineBreak(lb) {

			restoreMeasuringNodes();

			// Flag this to signify that meaure nodes are occupied.
			measurePostTextNode = {};

			measureSpanTextNode.nodeValue = _NBSP;
			_insertAfter(measureSpanEl, lb);

			var spatInf = _getPositionInWindow(measureSpanEl);
			spatInf.height = measureSpanEl.offsetHeight;

			measureSpanEl.parentNode.removeChild(measureSpanEl);

			// Reset flag
			measurePostTextNode = null;

			return spatInf;
		}

		/**
		 * Remeasures a cInfo's position (window and document) at its given
		 * textnode / charactor, index - and updates the info's properties
		 * accordingly.
		 * 
		 * @param {de.cursor.CursorDescriptor}
		 *            cursorDesc
		 */
		function setSpatialMembers(cursorDesc) {

			if (!cursorDesc)
				return;

			if (_nodeName(cursorDesc.domNode) == "br") {
				var inf = measureLineBreak(cursorDesc.domNode);
				cursorDesc.x = inf.x;
				cursorDesc.y = inf.y;
				cursorDesc.height = inf.height;
				cursorDesc.width = 0;

			} else {

				var spatEl;

				// Determine which element to get the spatial info from
				if (cursorDesc.placement == de.cursor.PlacementFlag.INSIDE) {

					setupMeasuringNodes(cursorDesc.domNode);

					measureCharactor(cursorDesc.relIndex);

					spatEl = measureSpanEl;

				} else
					spatEl = cursorDesc.domNode;

				var pos = _getPositionInWindow(spatEl);
				cursorDesc.x = pos.x;
				cursorDesc.y = pos.y;
				cursorDesc.width = spatEl.offsetWidth;
				cursorDesc.height = spatEl.offsetHeight;

			}

			var docScrollPos = _getDocumentScrollPos();
			cursorDesc.docLeft = docScrollPos.left + cursorDesc.x;
			cursorDesc.docTop = docScrollPos.top + cursorDesc.y;

			restoreMeasuringNodes();
		}

		// see de.cursor.getNextCursorMovement
		function getNextCursorMovement(srcCDesc, left) {

			var startNode = srcCDesc.domNode, startIndex = srcCDesc.relIndex, startIsRightOf = srcCDesc.isRightOf, nextNode, nextIndex, nextIsRightOf, placementFlags = getPlacementFlags(srcCDesc.domNode), lastVisitedNode, pendingLineBreak, seenBlockElement = false, prevTextInfo;

			if (placementFlags == de.cursor.PlacementFlag.INSIDE) {

				// If needs simple flip of is rightof flag then return a flipped
				// version
				if (srcCDesc.isRightOf == left) {
					var cDesc = de.cursor.createCursorDesc(srcCDesc.domNode,
							srcCDesc.relIndex, !srcCDesc.isRightOf);
					if (cDesc)
						return cDesc;
				}

			} else if (srcCDesc.isRightOf
					&& placementFlags != de.cursor.PlacementFlag.INSIDE) {

				if (left) {

					// If scanning left but source cursor is to the right of an
					// element, then need to start the
					// traversal within the elements deepest-right descendant
					while (startNode.lastChild) {
						startNode = startNode.lastChild;
					}
					if (startNode != srcCDesc.domNode) {
						startIndex = _nodeLength(startNode, 2) - 1;
						startIsRightOf = true;
					}

				}
			}

			// Begin traversing from source point inclusive
			_visitAllNodes(
					docBody,
					startNode,
					!left,
					function(domNode) {

						var firstVisit = domNode == startNode;

						// Skip node that are not displayed / is protected
						if (!_isNodeDisplayed(domNode)
								|| de.doc.isProtectedNode(domNode)) {
							lastVisitedNode = domNode;
							return true;
						}

						if (!seenBlockElement && !firstVisit)
							seenBlockElement = _isBlockLevel(domNode);

						placementFlags = getPlacementFlags(domNode);

						// Check after nodes
						if (lastVisitedNode) {
							var commonAncestor = _getCommonAncestor(domNode,
									lastVisitedNode, true);
							var checkANodes = _getAncestors(left ? domNode
									: lastVisitedNode, commonAncestor, false);
							if (left) { // Need to search from top-down in left
										// search
								checkANodes.reverse();
								// Include the current node in the search if it
								// is an after node - except for line breaks
								if ((placementFlags & de.cursor.PlacementFlag.AFTER)
										&& !_isAncestor(domNode,
												lastVisitedNode)
										&& _nodeName(domNode) != "br")
									checkANodes.push(domNode);
							} else {
								if (!_isAncestor(lastVisitedNode, domNode)
										&& _nodeName(lastVisitedNode) != "br"
										&& !de.doc
												.isProtectedNode(lastVisitedNode)
										&& !(lastVisitedNode == startNode && startIsRightOf))
									checkANodes.push(lastVisitedNode);
							}

							for ( var i in checkANodes) {
								var node = checkANodes[i];

								// Is this node an actual after node?
								if (getPlacementFlags(node)
										& de.cursor.PlacementFlag.AFTER) {

									// Check for pending line break
									if (checkLineBreak(node))
										return false;

									// Found the next move
									nextNode = node;
									nextIndex = 1;
									nextIsRightOf = true;
									return false;
								}

								// Update block level flag
								seenBlockElement |= _isBlockLevel(node);
							}
						}

						// Is a dom node which is needing a placeholder?
						if (placementFlags == 0) {

							if (!de.doc.isNodePackaged(domNode)) {
								var phType = 0
								if (_doesNeedESPlaceholder(domNode))
									phType = 1;
								else if (_doesNeedMNPlaceholder(domNode))
									phType = 2;

								if (phType) {

									// Create missing placeholder and add it..
									// prevent the undo manager
									// from setting the cursor and if there is
									// any undo history then
									// group this with the last action... and if
									// the dontStoreInsertPHOps option is set
									// then
									// prevent the undo manager from storing the
									// operations all together
									de.UndoMan
											.execute(
													de.UndoMan.hasUndo() ? de.UndoMan.ExecFlag.GROUP
															: 0,
													"InsertHTML",
													_getOuterHTML(phType == 1 ? de.doc
															.createESPlaceholder(domNode)
															: de.doc
																	.createMNPlaceholder()),
													domNode,
													domNode.firstChild, 0);

									// Check for pending linebreak first
									if (!checkLineBreak(domNode.firstChild)) {
										// Set next cursor point to the created
										// placeholder
										nextNode = domNode.firstChild;
										nextIndex = 0;
										nextIsRightOf = false;
									}
									return false;

								}
							}

						} else if (placementFlags == de.cursor.PlacementFlag.INSIDE) { // AKA
																						// A
																						// text
																						// node

							setupMeasuringNodes(domNode);

							var relIndex = firstVisit ? startIndex
									: (left ? _nodeLength(domNode) - 1 : 0), isRightOf;

							// Determine is rightof flag
							if (firstVisit)
								isRightOf = startIsRightOf;
							else if (prevTextInfo) {
								// If measured a text position (start point was
								// a text node),
								// use its isrightof flag, unless seen a block
								// level
								// element.
								if (seenBlockElement) {
									prevTextInfo = null; // Reset to void
															// checking for a
															// line wrap
									isRightOf = left;
								} else
									isRightOf = prevTextInfo.isRightOf;
							} else
								isRightOf = left;

							// For each charactor in the run of text
							for (; (left && relIndex >= 0)
									|| (!left && relIndex < measureFullText.length); relIndex += left ? -1
									: 1) {

								// Measure the charactor. Skip non renderable
								// charactors
								if (!measureCharactor(relIndex)) {
									if (firstVisit)
										isRightOf = !isRightOf;
									firstVisit = false;
									continue;
								}

								// Determine the charactor position
								var inf = _getPositionInWindow(measureSpanEl);
								inf.height = measureSpanEl.offsetHeight;

								// If there is a pending line break, check to
								// see if it
								// occurs on the same line as the measured
								// renderable charactor
								if (checkLineBreak(measureSpanEl, inf))
									return false;

								// Check if the charactor is at the start or end
								// of a line (line wrap start/end)
								if (prevTextInfo) {
									debug.assert(!firstVisit);
									if (!isOnSameLine(inf, prevTextInfo)) {
										nextNode = domNode;
										nextIndex = relIndex;
										nextIsRightOf = !isRightOf;
										return false;
									}
								}

								// If this isnt the starting point then found
								// the next pos
								if (!firstVisit
										|| startNode != srcCDesc.domNode) {
									nextNode = domNode;
									nextIndex = relIndex;
									nextIsRightOf = isRightOf;
									return false;
								}
								// Note: no need to check to flip isRightOf flag
								// since this is checked before traversal

								// Re-setup the measuring nodes (if needs to)
								// due to line break measurements above
								setupMeasuringNodes(domNode);
								firstVisit = false;

								// Set prevTextInfo for testing for line wraps
								// on next text measure
								prevTextInfo = {
									domNode : domNode,
									isRightOf : isRightOf,
									y : inf.y,
									height : inf.height
								};

								// If the node is a placeholder, only count one
								// movement
								if (de.doc.isMNPlaceHolder(domNode)
										|| de.doc.isESPlaceHolder(domNode)) {
									prevDesc.isRightOf = false;
									break;
								}

							} // End loop: measuring each char in the run of
								// text

							// Must restore nodes to avoid traversal going into
							// measure nodes themselves
							restoreMeasuringNodes();

						} else if (placementFlags
								& de.cursor.PlacementFlag.BEFORE) {

							// Only set as before node if not the first visit or
							// if searching left and began to right of the node.
							if (!firstVisit
									|| (left && domNode == startNode && startIsRightOf)) {
								if (!checkLineBreak(domNode)) {
									nextNode = domNode;
									nextIsRightOf = false;
									nextIndex = 0;
								}
								return false;
							}

						} else if (_nodeName(domNode) == "br") { // Check for
																	// line
																	// breaks
																	// (Pure
																	// AFTER
																	// nodes)

							if (!firstVisit) {

								// Check for any pending line breaks
								if (checkLineBreak(domNode))
									return false;

								if (left) {
									// For LEFT searches check for pending line
									// breaks right away since the
									// right-node information is always
									// available
									pendingLineBreak = domNode;
									if (checkLineBreak(
											lastVisitedNode,
											prevTextInfo
													&& prevTextInfo.domNode == lastVisitedNode ? prevTextInfo
													: null))
										return false;

								} else { // Right
									// Set new pending line break
									pendingLineBreak = domNode;
								}

							}
						}

						lastVisitedNode = domNode;

						// For right searches, if the cursor begins to the right
						// on an element then avoid
						// traversing inside the descendants
						if (!left
								&& firstVisit
								&& srcCDesc.isRightOf
								&& placementFlags != de.cursor.PlacementFlag.INSIDE)
							return 1;

					}); // End traversal

			restoreMeasuringNodes();

			// Found a cursor?
			return nextNode ? de.cursor.createCursorDesc(nextNode, nextIndex,
					nextIsRightOf) : null;

			/**
			 * An inner support function. Determines whether a pending line
			 * break should be the next cursor position
			 * 
			 * WARNING: This may restore any current measuring nodes!
			 * 
			 * @param {Node}
			 *            ele The current element to check pending line breaks
			 *            against
			 * 
			 * @param {Object}
			 *            eleDesc Optional (created if not provided). A
			 *            descriptor, with at least the y and height members set
			 * 
			 * @return {Boolean} True iff the next move is a pending line break
			 *         (in which case next node/index will be set).
			 * 
			 */
			function checkLineBreak(ele, eleDesc) {

				// If there is a pending line break, check to see if it
				// occurs on the same line as this
				if (pendingLineBreak) {

					// Measure pending line break and element spatial qaulties
					var lbMeas = measureLineBreak(pendingLineBreak), eleDesc = eleDesc
							|| (_nodeName(ele) == "br" ? measureLineBreak(ele)
									: {
										y : _getPositionInWindow(ele).y,
										height : ele.offsetHeight
									});

					if (!isOnSameLine(lbMeas, eleDesc)) {
						// If this before after node is not on the same line as
						// the pending
						// line break, then count the line break as part of the
						// move.
						nextNode = pendingLineBreak;
						nextIndex = 1;
						nextIsRightOf = true;
						return true;
					}

					pendingLineBreak = null;
				}

				return false;

			} // End inner checkLineBreak function

		} // End getNextCursorMovement

		// See de.cursor.getCursorDescAtXY
		function getCursorDescAtXY(targetX, targetY, targetNode) {

			if (!targetNode)
				targetNode = _getRenderedNodeAtXY(targetX, targetY);

			// If the target was the cursor - recalc the target to the node
			// behind the cursor.
			if (targetNode == cursorDiv) {
				cursorDiv.style.display = "none";
				targetNode = _getRenderedNodeAtXY(targetX, targetY);
				cursorDiv.style.display = "";
			}

			if (!targetNode)
				return null;

			// Get the searchspace for the bin search
			var searchSpace = getBinSearchSpace();

			// Perform the dual binary search
			var cDesc = searchBestCursorPos(targetX, targetY, searchSpace);

			// Restore the DOM structure
			restoreMeasuringNodes();

			return cDesc;

			/**
			 * An inner supporting function.
			 * 
			 * @return The search space for the binary search
			 */
			function getBinSearchSpace() {

				// Build array of all nodes to search inside of targetEl
				var nodesToSearch = [], totalPlacementLength = 0, wndSize = _getWindowSize();

				(function traverse(domNode) {

					var placementFlags = getPlacementFlags(domNode), checkElement, posInWindow;

					// Avoid adding nodes which are not in the viewport
					if (domNode.nodeType == Node.ELEMENT_NODE) {
						checkElement = domNode;
					} else if (domNode.nodeType == Node.TEXT_NODE
							&& placementFlags != 0) {
						setupMeasuringNodes(domNode);
						measurePreTextNode.nodeValue = "";
						measureSpanTextNode.nodeValue = measureFullText;
						measurePostTextNode.nodeValue = "";
						checkElement = measureSpanEl;
					}

					if (checkElement) {

						posInWindow = _nodeName(checkElement) == "br" ? measureLineBreak(checkElement)
								: _getPositionInWindow(checkElement);

						// Is this element above the veiw port?
						if ((posInWindow.y + checkElement.offsetHeight) <= 0) {
							restoreMeasuringNodes();
							return true;
						}

						// Is this element below the veiw port?
						if (posInWindow.y > wndSize.height) {
							restoreMeasuringNodes();
							return false;
						}

						restoreMeasuringNodes();
					}

					if (placementFlags == de.cursor.PlacementFlag.INSIDE) { // AKA
																			// a
																			// text
																			// node
						nodesToSearch.push({
							domNode : domNode,
							startIndex : totalPlacementLength,
							endIndex : totalPlacementLength
									+ _nodeLength(domNode) - 1,
							length : _nodeLength(domNode),
							placement : de.cursor.PlacementFlag.INSIDE
						});

						totalPlacementLength += _nodeLength(domNode);

					} else if (placementFlags & de.cursor.PlacementFlag.BEFORE) {
						nodesToSearch.push({
							domNode : domNode,
							startIndex : totalPlacementLength,
							endIndex : totalPlacementLength + 1,
							length : 1,
							placement : de.cursor.PlacementFlag.BEFORE, // Only
																		// store
																		// before
																		// flag
							posInWnd : posInWindow
						// cache this
						});
						totalPlacementLength++;
					}

					// Recurse in order traversal
					var child = domNode.firstChild;
					var continueTrav = true;
					while (child) {
						if (!traverse(child)) {
							continueTrav = false; // Started to traverse in
													// node below the viewport
							break;
						}
						child = child.nextSibling;
					}

					// Add after nodes (event if aborting traversal)
					if (placementFlags & de.cursor.PlacementFlag.AFTER) {
						nodesToSearch.push({
							domNode : domNode,
							startIndex : totalPlacementLength,
							endIndex : totalPlacementLength + 1,
							length : 1,
							placement : de.cursor.PlacementFlag.AFTER, // Only
																		// store
																		// after
																		// flag
							posInWnd : posInWindow
						// cache this
						});
						totalPlacementLength++;

					}

					return continueTrav;

				})(targetNode);

				return {
					nodes : nodesToSearch,
					totalLength : totalPlacementLength
				};

			} // End inner getBinSearchSpace

		} // End getCursorDescAtXY

		/**
		 * IMPORTANT: Measurement nodes are left un-restored after this
		 * operation Call restoreMeasuringNodes if you want to restore the dom.
		 * 
		 * @param {Number}
		 *            targetX The x coord to get the closest cusor pos to
		 * @param {Number}
		 *            targetY The y coord to get the closest cusor pos to
		 * @param {Object}
		 *            A dual bin search space to search.
		 * @param {de.cursor.CursorDescriptor}
		 *            targetLineRef A reference point to search for a best
		 *            position directly above or below the line. Null for full
		 *            search.
		 * 
		 * @param {Boolean}
		 *            aboveLine If given targetLineRef then set to true to
		 *            search for best position above the reference point. Other
		 *            false will search below the reference point.
		 * 
		 * @return {de.cursor.CursorDescriptor} The closest cursor position it
		 *         can find. Null if could not find one.
		 * 
		 */
		function searchBestCursorPos(targetX, targetY, searchSpace,
				targetLineRef, aboveLine) {

			// Hash table as an associative array, caches measurements
			var nonRenderables = {};

			if (searchSpace.totalLength == 0)
				return null;

			// Sample first position in target element
			var startDesc = getCursorDescFrom(0, 0, 2);
			if (!startDesc)
				return null;

			// Sample last charactor in target element
			var endDesc = getCursorDescFrom(searchSpace.nodes.length - 1,
					searchSpace.nodes[searchSpace.nodes.length - 1].length - 1,
					1);

			debug.assert(endDesc != null);

			// Check to see if first and last samples are the same
			if (startDesc.domNode == endDesc.domNode
					&& startDesc.absIndex == endDesc.absIndex) {
				// There must be only 1 renderable charactor in the target
				// element
				return validDescriptor(startDesc);
			}

			// Store the first samples
			var samples = [ startDesc, endDesc ];

			var best = null;

			// Determine closest sample and set as the current best
			best = isCloserToTarget(startDesc, endDesc, targetY) ? startDesc
					: endDesc;

			// Upper and lower are the bounds of the search space in the form of
			// cursor descriptors
			var upper, lower;

			// This algorithm has two passes: The first pass is a binary search
			// to discover the line, or closest line,
			// that the target is on. The second pass is a binary search to home
			// in on the closest charactor to the target
			// on the line that was found to be the best.
			for (var pass = 1; pass <= 2; pass++) { // dual binary search
				if (pass == 1) { // setup first pass: Y DOMAIN

					if (targetLineRef) {

						// If the binary search should find best matching
						// position above or below a line reference point,
						// then discover all lines within the search space
						discoverAllLines(startDesc,
								getNodeIndex(startDesc.absIndex), endDesc,
								getNodeIndex(endDesc.absIndex));

						// Select the closest sample that does not fall on the
						// line reference point
						selectBest();

						// Now the the target line has been discovered, set the
						// target Y
						targetY = best.y + (best.height / 2);

						// Re-calc best abs delta y
						best.ady = Math.abs(best.y - targetY);

						// Move to X-bin-search
						continue;

					} else {
						// Select the upper and low for the Y range - goto next
						// pass if already on target line
						if (!selectYRange())
							continue;
					}

				} else { // setup second pass: X DOMAIN
					var res = selectXRange();

					if (typeof res == "boolean") {
						if (!res)
							break; // Else the upper and lower range has been
									// selected
					} else
						break; // the best was found
				}

				// Enter binary search for quickly locating closest line, or
				// charactor
				while (true) {

					debug.assert(lower.absIndex < upper.absIndex);

					// If lower is next to upper and was doing the line search,
					// then the line search is done... an exact match wasn't
					// found
					// and the binary search verged towards two charactors side
					// by side but on different lines.
					// ...and if was doing the charactor search, then the search
					// has verged at the final point
					if (lower.absIndex == (upper.absIndex - 1))
						break;

					// Determine current index by halving the search space
					var curAbsIndex = lower.absIndex
							+ Math.floor((upper.absIndex - lower.absIndex) / 2);

					// Ensure that the index is not out of bounds
					if (curAbsIndex == lower.absIndex)
						curAbsIndex++;
					else if (curAbsIndex == upper.absIndex)
						curAbsIndex--;

					// Locate which node within the target element that the
					// current absolute index is in
					var curNodeIndex = getNodeIndex(curAbsIndex);

					// Get the cursor desc at the current node/rel-index
					var current = getCursorDescFrom(curNodeIndex, curAbsIndex
							- searchSpace.nodes[curNodeIndex].startIndex, 0);

					// Upper and Lower are next to each other
					if (!current)
						break;

					// In the first pass all samples are recorded - the initial
					// upper and lower bounds of the next pass
					// will be salvaged from these samples.
					if (pass == 1)
						samples.push(current);

					// Check to see if current is the new best
					if (isCloserToTarget(current, best, targetY))
						best = current;

					if (pass == 1) { // Line search
						// Check to see if current matches line.
						if (targetY >= current.y
								&& targetY <= (current.y + current.height))
							break; // finished since found a match

						// Narrow search
						else if (current.y > targetY)
							upper = current;
						else
							lower = current;

					} else { // Charactor search
						// Determine if current is on the same line as the best
						if (isOnSameLine(current, best)) {

							// See if target X is on top of current
							if (targetX >= current.x
									&& targetX <= (current.x + current.width))
								break; // If so, then search complete

							// Otherwise narrow search based on x position
							else if (current.x > targetX)
								upper = current;
							else
								lower = current;

						} // If not on same line, then narrow search based on
							// Y coordinates
						else if (current.y > targetY)
							upper = current;
						else
							lower = current;

					}

				} // End loop: core binary search for finding line and
					// charactor
			} // End passes
			// FINISHED
			return validDescriptor(best);

			// Support functions to follow...

			/**
			 * @param {Number}
			 *            absIndex The abs index in the search-space
			 * 
			 * @return {Number} The index within nodes that absIndex resides
			 */
			function getNodeIndex(absIndex) {

				var nodes = searchSpace.nodes;

				// If there is one text node, then clearly the current index is
				// that node.
				if (nodes.length == 1)
					return 0;

				// Is it in the first text node?
				if (absIndex >= nodes[0].startIndex
						&& absIndex <= nodes[0].endIndex)
					return 0;

				// Is in last text node?
				if (absIndex >= nodes[nodes.length - 1].startIndex
						&& absIndex <= nodes[nodes.length - 1].endIndex)
					return nodes.length - 1;

				// begin a little binary search to quickely find the node in the
				// search space
				var lo = 0;
				var up = nodes.length - 1;
				var nodeIndex;

				while (true) {

					var cur = lo + Math.floor((up - lo) / 2);
					if (cur == lo)
						cur++;
					else if (cur == up)
						cur--;

					if (absIndex >= nodes[cur].startIndex
							&& absIndex <= nodes[cur].endIndex) {
						nodeIndex = cur;
						break;

					} else if (absIndex < nodes[cur].startIndex)
						up = cur; // search downward

					else
						lo = cur; // search upward
				} // End loop: binary search for locating #text node
				return nodeIndex;

			} // End inner getNodeIndex

			/**
			 * Gets a cursor descriptor from a given position in the search
			 * space.
			 * 
			 * Some text nodes in the search space may not support a cursor
			 * placement since they can contain only non-renderable symbols.
			 * Therefore the returned cursor desc may not be at the given
			 * node/index.
			 * 
			 * @param {Number}
			 *            dir 0 = Both, within upper and lower bounds, 1 = Left
			 *            only, 2 = Right only.
			 * @param {Number}
			 *            relIndex The relative index
			 * @param {Number}
			 *            nodeIndex The index within the search space nodes
			 * 
			 * @return {Object} the cursor descriptor or NULL if did not find a
			 *         cursor at the given position that is in bounds.
			 */
			function getCursorDescFrom(nodeIndex, relIndex, dir) {

				var nodes = searchSpace.nodes,

				searchLeft = dir == 0 || dir == 1,

				// Store the current relative/node index
				// for restoring when switching scan direction
				origialRelIndex = relIndex, originalNodeIndex = nodeIndex,

				// Ideally we would directly measure spatial info at the current
				// node / relative index.
				// However some charactors in text nodes aren't renderable, and
				// thus we must scan left and/or right
				// to find next renderable charactor
				measureEl = null, node; // the node within the searchspace nodes

				// Find first renderable symbol. 1 pass for non-text nodes,
				// 1-2 pass for text nodes and searching both dirs: 1st pass
				// search left, 2nd pass search right.
				do {

					if (!searchLeft && dir == 0) { // 2nd pass?
						// Switching direction...
						nodeIndex = originalNodeIndex;
						relIndex = origialRelIndex + 1; // exclusive
						// Check to see if the right of the starting point is in
						// a different node
						if (relIndex > nodes[nodeIndex].endIndex) {
							nodeIndex++;
							relIndex = 0;
							// Note: If the nodeIndex is out of bounds, the next
							// loop will instantly break
						}
					}

					var reachedSSBounds = false; // refers to search-space
													// upper/lower bounds

					// Scan through the nodes
					while (nodeIndex >= 0 && nodeIndex < nodes.length) {

						node = nodes[nodeIndex];

						// Ignore non-diplayed nodes
						if (_isNodeDisplayed(node.domNode)) {

							// Discover the type of node
							if (node.placement == de.cursor.PlacementFlag.INSIDE) { // AKA
																					// Text
																					// node
								// Setup measurement nodes for this text node we
								// are about to search in
								setupMeasuringNodes(node.domNode);

								// Scan through charactors.. looking for the
								// first renderable symbol
								while (relIndex >= 0
										&& relIndex < measureFullText.length) {

									// Is the text node/index out of bounds
									// (only when scanning both ways)?
									if (dir == 0
											&& ((lower.domNode == node.domNode && lower.relIndex == relIndex) || (upper.domNode == node.domNode && upper.relIndex == relIndex))) {
										reachedSSBounds = true;
										break;
									}

									// or have we measured this before and found
									// it was not a renderable char?
									if (nonRenderables["_" + nodeIndex + '_'
											+ relIndex]) {
										relIndex += (searchLeft ? -1 : 1);
										continue; // avoid re-measuring
													// unrenderable node
									}

									// Measure the current node / index.
									if (!measureCharactor(relIndex)) {
										// Char is not renderable, note the
										// element and store in the
										// hash table (using an accoiative
										// array)
										nonRenderables["_" + nodeIndex + '_'
												+ relIndex] = true;
										relIndex += (searchLeft ? -1 : 1);
										continue;
									}

									// Determine position of the charactor. This
									// will be used as a flag
									// for ending the search for the first
									// non-renderable charactor
									measureEl = measureSpanEl;
									break;

								} // End loop: Searching for renderable
									// charactor

							} else { // BEFORE and AFTER nodes
								// Is the text node/index out of bounds (only
								// when scanning both ways)?
								if (dir == 0
										&& ((lower.domNode == node.domNode && lower.placement == node.placement) || (upper.domNode == node.domNode && upper.placement == node.placement))) {
									reachedSSBounds = true;
									break;
								}

								measureEl = node.domNode;
								break;
							}
						}

						// Have we found the next sample, or has the right-scan
						// reached the upper search-space bound?
						if (measureEl || reachedSSBounds)
							break;

						// Setup current node index for scanning the next node
						nodeIndex += (searchLeft ? -1 : 1);

						// Setup relative index for searching for next
						// renderable char
						relIndex = (searchLeft && nodeIndex >= 0 && nodeIndex < nodes.length) ? (nodes[nodeIndex].placement == de.cursor.PlacementFlag.INSIDE ? nodes[nodeIndex].length - 1
								: 1)
								: 0;

					} // End loop: scanning nodes in a particular direction

					// Was a sample found?
					if (measureEl)
						break;

				} while (dir == 0 && !(searchLeft = !searchLeft)); // End loop:
																	// scanning
																	// left
																	// and/or
																	// right

				// If there were no places where the cursor can be placed
				// between lower and upper,
				// then the search is done.
				if (!measureEl)
					return null;

				var pos, width, height;

				// Is their cached spatial calculations for this node?
				if (node.posInWnd)
					pos = node.posInWnd;

				if (_nodeName(measureEl) == "br") {
					pos = pos || measureLineBreak(measureEl);
					height = pos.height;
					width = 0;
				} else {
					pos = pos || _getPositionInWindow(measureEl);
					width = measureEl.offsetWidth;
					height = measureEl.offsetHeight;
				}

				var adxl = Math.abs(pos.x - targetX), adxr = Math.abs(pos.x
						+ width - targetX), isRightOf;

				switch (node.placement) { // searchspace placement flags are
											// separated (cannot have combined
											// flags)
				case de.cursor.PlacementFlag.BEFORE:
					isRightOf = false;
					break;

				case de.cursor.PlacementFlag.AFTER:
					isRightOf = true;
					break;

				default: // INSIDE/TEXT
					isRightOf = adxr < adxl;
				}

				return {
					domNode : node.domNode,
					relIndex : relIndex,
					absIndex : node.startIndex + relIndex,
					placement : node.placement,
					isRightOf : isRightOf,
					x : pos.x,
					y : pos.y,
					adx : isRightOf ? adxr : adxl,
					ady : Math.min(Math.abs(pos.y - targetY), Math.abs(pos.y
							+ measureEl.offsetHeight - targetY)),
					width : width,
					height : height
				};

			} // End inner getCursorDescFrom

			/**
			 * @return {Boolean} True if the range is set. False if the start or
			 *         end sample is on the targets line
			 */
			function selectYRange() {

				// If the start or end sample is not on the target line, and the
				// target is within the samples y-range,
				// then find the best line
				if (!((targetY >= startDesc.y && targetY <= (startDesc.y + startDesc.height)) || (targetY >= endDesc.y && targetY <= (endDesc.y + endDesc.height)))
						&& targetY >= startDesc.y
						&& targetY <= (endDesc.y + endDesc.height)) {

					// If the target Y does not occur on the start or end
					// sample's line:
					lower = startDesc;
					upper = endDesc;

					return true;
				}

				return false; // Otherwise, the line-search is done! Next
								// pass...
			} // End inner selectYRange
			/**
			 * @return {Boolean, de.cursor.CursorDescriptor} False if the best
			 *         is right at the target. Or True if the range has been
			 *         selected. Or A cursor descriptor of the best match if
			 *         found while setting the range - in which case the best
			 *         var is set
			 * 
			 */
			function selectXRange() {

				// Determine the lower and upper bounds to start the charactor
				// binary search with
				if (targetX >= best.x && targetX < (best.x + best.width)) {
					// If the best is right at the target, then the search is
					// done
					return false;

				} else if (best.x > targetX) { // search to left of best
					upper = best;
					lower = null;
					for (i in samples) {
						current = samples[i];
						/*
						 * 
						 * if (current == best) continue;
						 * 
						 * 
						 * var isLeftOrAbove;
						 *  // Check if this sample (current) is to the left or
						 * above of best if (isOnSameLine(current, best)) { //
						 * If sample is on sample line as best, then check X
						 * coords isLeftOrAbove = current.x < best.x;
						 *  } else isLeftOrAbove = current.y < best.y; // Check
						 * Y coords if not on same line // If the sample is to
						 * the left, or above, of best. Then check to see if the
						 * sample // is closer than the current lower to best.
						 * if (isLeftOrAbove && (!lower || isCloserToXY(current,
						 * lower, best.x, best.y)) && current.absIndex <
						 * upper.absIndex) { lower = current; }
						 */

						// Select preceeding sample to best in sample set
						if (current.absIndex < best.absIndex
								&& (!lower || current.absIndex > lower.absIndex))
							lower = current;

					}

					if (!lower) {
						// The best line must have been the starting sample,
						// thus the best charactor is
						// the first renderable charactor.
						return best;
					}

				} else { // Search to the right of best
					lower = best;
					upper = null;
					for (i in samples) {

						current = samples[i];

						/*
						 * var isRightOrBelow;
						 *  // Check if this sample (current) is to the right or
						 * below of best if (isOnSameLine(current, best)) { //
						 * If sample is on sample line as best, then check X
						 * coords isRightOrBelow = current.x > best.x;
						 *  } else isRightOrBelow = current.y > best.y; // Check
						 * Y coords if not on same line // If the sample is to
						 * the right, or below, of best. Then check to see if
						 * the sample // is closer than the current upper to
						 * best. if (isRightOrBelow && (!upper ||
						 * isCloserToXY(current, upper, (best.x + best.width),
						 * best.y)) && current.absIndex > lower.absIndex) {
						 * upper = current; }
						 */

						if (current.absIndex > best.absIndex
								&& (!upper || current.absIndex < upper.absIndex))
							upper = current;

					}

					if (!upper) {
						// The best line must have been the ending sample, thus
						// the best charactor is
						// the first renderable charactor.
						return best;
					}

				}

				return true;

			} // End inner selectXRange

			/**
			 * Sets the best local for searching for the closest cursor position
			 * before/after a line reference point.
			 */
			function selectBest() {

				// Begin with the search space bounds... depending on whether
				// the search should
				// look above or below the line reference point
				best = aboveLine ? startDesc : endDesc;

				// Look in all samples... which contain all lines
				for ( var i in samples) {

					var sample = samples[i];

					// Skip samples that are either on the same line as the
					// reference line, or
					// is out of bounds...
					if (isOnSameLine(sample, targetLineRef)
							|| (aboveLine && sample.y > targetLineRef.y)
							|| (!aboveLine && sample.y < targetLineRef.y))
						continue;

					// Set new best if sample is better
					if (isCloserToTarget(sample, best, targetY)) {
						best = sample;
					}

				}

			} // End inner selectBest

			/**
			 * Samples the search space to discover all lines.
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            lo A cursor desc
			 * @param {de.cursor.CursorDescriptor}
			 *            up A cursor desc
			 * 
			 * @param {Number}
			 *            lni Lower node index in search space
			 * @param {Number}
			 *            uni Upper node index in search space
			 */
			function discoverAllLines(lo, lni, up, uni) {

				var stack = [ [ lo, lni, up, uni ] ];

				while (stack.length > 0) { // simulating recursion - faster and
											// avoids stack overflows

					var args = stack.pop();
					lo = args[0];
					lni = args[1];
					up = args[2];
					uni = args[3];

					// Special attention must be payed to tables. They contain
					// inner lines.
					var ni = lni;

					// Shrink lower range if lower is a table
					while (ni < searchSpace.nodes.length
							&& _nodeName(searchSpace.nodes[ni].domNode) == "table") {
						ni++;
					}

					if (ni == searchSpace.nodes.length)
						continue;

					if (ni != lni) {
						lo = getCursorDescFrom(ni, 0, 2);
						lni = ni;
						samples.push(lo); // duplicates are ok
					}

					ni = uni;

					// Shrink upper range if upper is a table
					while (ni >= 0
							&& _nodeName(searchSpace.nodes[ni].domNode) == "table") {
						ni--;
					}

					if (ni == -1)
						continue;

					if (ni != uni) {
						up = getCursorDescFrom(ni,
								searchSpace.nodes[ni].length - 1, 1);
						uni = ni;
						samples.push(up); // duplicates are ok
					}

					// If lower is directly next to upper or they are on the
					// same line
					// then the line discovery between these two points is
					// complete
					if (lo.absIndex >= (up.absIndex - 1)
							|| isOnSameLine(up, lo))
						continue;

					// Determine middle index by halving the search space
					var midIndex = lo.absIndex
							+ Math.floor((up.absIndex - lo.absIndex) / 2);

					// Ensure that the index is not out of bounds
					if (midIndex == lo.absIndex)
						midIndex++;
					else if (midIndex == up.absIndex)
						midIndex--;

					// Locate which node within the target element that the
					// current absolute index is in
					var midNodeIndex = getNodeIndex(midIndex);

					// Get the cursor desc at the mid point
					lower = lo; // getCursorDescFrom uses this for boundry
								// checks
					upper = up; // getCursorDescFrom uses this for boundry
								// checks
					var mid = getCursorDescFrom(midNodeIndex, midIndex
							- searchSpace.nodes[midNodeIndex].startIndex, 0);

					// Upper and Lower are next to each other
					if (!mid)
						continue;

					// Record the sample
					samples.push(mid);

					// Simulate recursion using a local stack
					stack.push([ lo, lni, mid, midNodeIndex ]); // left side
					stack.push([ mid, midNodeIndex, up, uni ]); // right side

				} // Next

			} // End inner discoverAllLines

			/**
			 * Esnures that the given descriptor is valid.
			 * 
			 * @param {de.cursor.CursorDescriptor}
			 *            cDesc A desciptor
			 * 
			 * @return If the given descriptor wasn't valid, it returns a
			 *         neighbouring cursor placement, otherwise the given
			 *         descriptor is returned.
			 */
			function validDescriptor(cDesc) {
				if (!cDesc)
					return null;

				// If the search ended on a line break, then check to make sure
				// that the line break has no cursor placements to the left or
				// right of it...
				if (_nodeName(cDesc.domNode) == "br") {
					var prevDesc = getNextCursorMovement(cDesc, true);
					if (prevDesc && isOnSameLine(cDesc, prevDesc)) {
						cDesc = prevDesc;
						cDesc.isRightOf = true;
					} else {
						var nextDesc = getNextCursorMovement(cDesc, false);
						if (nextDesc && isOnSameLine(cDesc, nextDesc)) {
							cDesc = nextDesc;
							cDesc.isRightOf = false;
						}
					}
				} else if (de.doc.isMNPlaceHolder(cDesc.domNode))
					cDesc.isRightOf = false;

				cDesc.placement = getPlacementFlags(cDesc.domNode);

				// Supply the position of the cursor in the actual document
				// rather than just within the window.
				var docScrollPos = _getDocumentScrollPos();

				cDesc.docLeft = docScrollPos.left + cDesc.x;
				cDesc.docTop = docScrollPos.top + cDesc.y;

				return cDesc;
			} // End inner validDescriptor

		} // End searchBestCursorPos

	})();
	// End Cursor.js
	// Start Doc.js
	var
	/**
	 * @final The protected node classname prefix
	 * @type String
	 */
	_PROTECTED_CLASS = "sw-protect",

	/**
	 * @final The editable section node classname prefix
	 * @type String
	 */
	_ES_CLASS_PREFIX = "editable",

	/**
	 * @final The classname use for packaged nodes
	 * @type String
	 */
	_PACKAGE_CLASS_NAME = "sw-packaged";

	/**
	 * Note: may want to check _doesNeedESPlaceholder first.
	 * 
	 * @param {Node}
	 *            domNode A dom node in the document to test.
	 * @return {Boolean} True if the given dom node is in need of a modifiable
	 *         placeholder.
	 * 
	 * @see _doesNeedESPlaceholder
	 */
	function _doesNeedMNPlaceholder(domNode) {
		if (_isPlaceholderCandidate(domNode))
			return !_doesContainTanglableDescendant(domNode);
		return false;
	}

	/**
	 * @param {Node}
	 *            domNode A dom node in the document to test.
	 * @return {Boolean} True if the given dom node is in need of a editable
	 *         section placeholder.
	 * 
	 * @see _doesNeedMNPlaceholder
	 */
	function _doesNeedESPlaceholder(domNode) {
		if (de.doc.isEditSection(domNode))
			return !_doesContainTanglableDescendant(domNode);
		return false;
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff the dom node contains a descendant for which
	 *         the cursor can be placed by.
	 */
	function _doesContainTanglableDescendant(domNode) {

		var containsTangableNode = false;

		// Check if needs a placeholder
		_visitAllNodes(
				domNode,
				domNode,
				true,
				function(node) {

					if (node == domNode || de.doc.isProtectedNode(node))
						return;

					var pflags = de.cursor.getPlacementFlags(node);
					if (pflags == de.cursor.PlacementFlag.INSIDE) {

						if (_doesTextSupportNonWS(node)
								&& _nodeLength(node) > 0) {

							if (_isAllWhiteSpace(node.nodeValue)) {

								// Validate that the text node is tangable
								var measureSpan = $createElement("span"), measureText = document
										.createTextNode(node.nodeValue);

								measureSpan.appendChild(measureText);
								node.nodeValue = "";
								node.parentNode.appendChild(measureSpan);
								containsTangableNode = measureSpan.offsetHeight != 0
										&& measureSpan.offsetWidth != 0;
								node.parentNode.removeChild(measureSpan);
								node.nodeValue = measureText.nodeValue;

								// TODO: Opera sometimes (randomly) incorrectly
								// sets the offset width/height to zero
								// on text nodes...

							} else
								containsTangableNode = true;
						}
					} else if (pflags)
						containsTangableNode = true;

					return !containsTangableNode;
				});

		return containsTangableNode;
	} // End function doesContainTanglableDescendant

	/**
	 * 
	 * @param {Node}
	 *            node A dom node to test
	 * @return {Undefined, Boolean} True if node is a placeholder candidate.
	 *         Undefined if it is not.
	 */
	var _isPlaceholderCandidate = function() {
		/*
		 * A map containing elements the cursor can navigate into which do not
		 * contain a "Tangle node." Excludes body
		 */
		var placeholderCandidates = $createLookupMap("li,dd,dt,p,td,th,h1,h2,h3,h4,h5,h6,pre,div");

		return function(node) {
			return placeholderCandidates[_nodeName(node)] || node == docBody;
		}
	}();

	(function() {

		$enqueueInit("Doc", function() {

			// Make as subject
			_model(de.doc);

			// Preprocess the document: consolidate all existing/initial
			// editable sections
			var es = de.doc.getAllEditSections();

			_recordOperations = false;
			for ( var i in es) {
				_consolidateWSSeqs(es[i], true);
			}
			_recordOperations = true;

			// For dynamically added editable sections, consolidate them too.
			de.doc.addObserver({
				onSectionAdded : function(editSection) {
					// Only consolidate within the editable sections to avoid
					// possibilities of consoldiating
					// surrounding editable DOM with Undo/Redo history.
					_recordOperations = false;
					_consolidateWSSeqs(editSection, false);
					_recordOperations = true;
				}
			});

		});

		var propertySetMap = {}, MN_PH_CLASS = "sw-mn-ph", ES_PH_CLASS = "sw-es-ph", ES_CLASS_TEST_REGEXP = new RegExp(
				"^" + _ES_CLASS_PREFIX + ".*$"), ES_CLASS_MATCH_REGEXP = new RegExp(
				"^" + _ES_CLASS_PREFIX + "-?(.+)$"), PROTECTED_NODE_TEST_REGEXP = new RegExp(
				"^" + _PROTECTED_CLASS + "$"), PACKAGED_NODE_TEST_REGEXP = new RegExp(
				"^" + _PACKAGE_CLASS_NAME + "$");

		// Create the doc namespace

		/**
		 * @namespace Provides CSS like language for declaring and customizing
		 *            editable sections on web pages.
		 * 
		 * <br>
		 * <br>
		 * Whenever a new editable section has been dynamically added to the
		 * document, a "onSectionAdded" event is fired, where the argument is
		 * the added editable section.
		 * 
		 * <br>
		 * <br>
		 * Whenever a editable section has been dynamically removed from the
		 * document, a "onSectionRemoved" event is fired, where the argument is
		 * the removed editable section.
		 * 
		 * @borrows de.mvc.AbstractSubject#addObserver as this.addObserver
		 * 
		 * @borrows de.mvc.AbstractSubject#removeObserver as this.removeObserver
		 * 
		 * @author Brook Novak
		 */
		de.doc = {

			/**
			 * @param {Node}
			 *            node A dom node
			 * 
			 * @return {Node} The first ancestor element of node which is an
			 *         editable section, inclusive of the given node itself.
			 *         Null if the node / none of its ancestors are editable
			 *         sections.
			 */
			getEditSectionContainer : function(node) {
				return _findAncestor(node, null, this.isEditSection, true);
			},

			/**
			 * Determines whether a dom node is marked as edit section element.
			 * 
			 * @param {Node}
			 *            node The dom node to test
			 * 
			 * @return {Boolean} True if node is a edit section element
			 */
			isEditSection : function(node) {
				if (node && node.nodeType == Node.ELEMENT_NODE)
					return _findClassName(node, ES_CLASS_TEST_REGEXP);
				return false;
			},

			/**
			 * @return {[Node]} An array of editable sections currently in the
			 *         document.
			 */
			getAllEditSections : function() {

				var editSections = [];
				_visitAllNodes(docBody, docBody, true, function(domNode) {
					if (de.doc.isEditSection(domNode))
						editSections.push(domNode);
				});
				return editSections;

			},

			/**
			 * @param {Node}
			 *            domNode A dom node to test
			 * @return {Boolean} True if the given dom node is a descendant of
			 *         an editable section.
			 */
			isNodeEditable : function(domNode) {
				var es = this.getEditSectionContainer(domNode);
				return es != null && es != domNode;
			},

			/**
			 * @param {Node}
			 *            domNode A dom node to test
			 * @return {Node} The protected nodes container is the node is
			 *         proected. If the dom is a container then it will be
			 *         returned. Otherwise null will be returned.
			 */
			getProtectedNodeContainer : function(domNode) {
				return _findAncestor(domNode, null,
						function(node) {
							return node.nodeType == Node.ELEMENT_NODE
									&& _findClassName(node,
											PROTECTED_NODE_TEST_REGEXP);
						}, true);
			},

			/**
			 * @param {Node}
			 *            domNode A dom node to test
			 * @return {Boolean} True if the given dom node is, or is an
			 *         descendant of, a protected node
			 */
			isProtectedNode : function(domNode) {
				return this.getProtectedNodeContainer(domNode) != null;
			},

			/**
			 * @param {Node}
			 *            domNode A dom node to test
			 * 
			 * @return {Node} The package root node. Null if the node is not
			 *         packaged.
			 */
			getPackageContainer : function(domNode) {
				return _findAncestor(domNode, null, function(node) {
					return node.nodeType == Node.ELEMENT_NODE
							&& _findClassName(node, PACKAGED_NODE_TEST_REGEXP);
				}, true);
			},

			/**
			 * A "packaged" node is part of a tree of nodes which are not
			 * allowed to be edited, although they are in an editable section.
			 * 
			 * @param {Node}
			 *            domNode A dom node to test
			 * @return {Boolean} True if the given dom node is part of a
			 *         package.
			 */
			isNodePackaged : function(domNode) {
				return this.getPackageContainer(domNode) != null;
			},

			/**
			 * Declares or overrides a property set for editable sections. <br>
			 * <br>
			 * Attributes:
			 * 
			 * actionFilter: "[!][actionname1[,actionname2[,...]]]" where actual
			 * name is case insensitive undoable action name. Format action can
			 * be followed by sub-action encapsulated in brackets <br>
			 * <br>
			 * If the property name exists, then the existing set will be
			 * overridden. You can override the default property set by using
			 * the name "defaultSet".
			 * 
			 * @example de.doc.declarePropertySet("metadata", { inputFilter:
			 *          "[[a-z][A-Z][1-9]\\s\\n]*", actionFilter:
			 *          "!blockquote,changecontainer,format(link)" Accept all
			 *          actions EXCEPT for blockquote,changecontainer,formatting
			 *          links
			 * 
			 * });
			 * 
			 * @see TODO REFER TO SPEC
			 * 
			 * @param {String}
			 *            name The name of the class being declared.
			 * 
			 * 
			 * @param {Object}
			 *            properties A set of attributes that is associated with
			 *            the given name.
			 * 
			 */
			declarePropertySet : function(name, properties) {
				properties = _clone(properties);

				// Build action filter
				if (typeof properties.actionFilter == "string") {

					var actionFilter = properties.actionFilter;

					// Is the action inclusive or exclusive?
					if (actionFilter.charAt(0) != '!') {
						properties.afInclusive = true;
						// Add implicit text editing actions
						if (actionFilter)
							actionFilter += ",";
						actionFilter += "inserthtml,inserttext,removedom,removetext";
					} else {
						actionFilter = actionFilter.substr(1);
						properties.afInclusive = false;
					}

					// Break filter into tokens
					var tokens = actionFilter.toLowerCase().split(',');

					// Build up the reg exp for quick filtering
					var reStr = "(";

					for ( var i in tokens) {

						reStr += ((i == '0') ? "" : "|");

						var token = tokens[i];

						var match = /^format\((.+)\)$/.exec(token);

						// Format actions can have sub-action filters
						if (match) {

							var subTokens = match[1].split(',');

							for ( var j in subTokens) {
								reStr += ((j == '0') ? "" : "|") + "format"
										+ subTokens[j];
							}

						} else {

							// Add the action name to the reg exp set
							reStr += token;

							if (token.indexOf("format") == 0)
								reStr += ".+"; // If not sub-format actions are
												// defined then declare as all
												// format actions
						}

					} // End loop: parsing action tokens

					reStr += ")";

					properties.afRE = new RegExp("^" + reStr + "$");
				}

				// Set or override a property set
				propertySetMap[name] = properties;

			},

			/**
			 * Declares a batch of property sets in a single call
			 * 
			 * @param {Object}
			 *            sets An object containing key-value pairs, when the
			 *            keys are property set names, and values are objects
			 *            containing properties.
			 */
			declarePropertySets : function(sets) {
				for ( var tuple in sets) {
					this.declarePropertySet(tuple, sets[tuple]);
				}
			},

			/**
			 * Retreives the editable property set for a given node. This
			 * considers property inheritance
			 * 
			 * @param {Node}
			 *            node A dom node
			 * 
			 * @return {Object} A set of read only dedit atrributes that the
			 *         given node has inherited. Null if the node is not a (or
			 *         descendant of a) editable section.
			 */
			getEditProperties : function(node) {

				// Get the editable section container for this node
				if (!this.isEditSection(node))
					node = this.getEditSectionContainer(node);

				if (node) {

					// Get the property set name for this node
					var esClassName = _findClassName(node, ES_CLASS_TEST_REGEXP);

					if (esClassName) {
						var nameMatch = ES_CLASS_MATCH_REGEXP.exec(esClassName);
						return nameMatch ? propertySetMap[nameMatch[1]] || {}
								: {};
					}

					return {};

				}

				return null;
			},

			/**
			 * @return {Node} A modifiable node placeholder element.
			 * 
			 * @see The DOM-based Web Editor Specification 1.0: Section 1.4
			 */
			createMNPlaceholder : function() {
				var ph = $createElement("span");
				_setClassName(ph, MN_PH_CLASS);
				ph.innerHTML = "&nbsp;";
				return ph;
			},

			/**
			 * Determines whether a dom node is (part of) a modifiable node
			 * placeholder.
			 * 
			 * @param {Node}
			 *            node A dom node to test
			 * 
			 * @param {Boolean}
			 *            immediate True to only test if node is a placeholder.
			 *            False to also test if nodes parent is a placeholder.
			 * 
			 * @return {Boolean} True if node is (part of) a modifiable node
			 *         placeholder. False if it is not
			 * 
			 * @see The DOM-based Web Editor Specification 1.0: Section 1.4
			 */
			isMNPlaceHolder : function(node, immediate) {
				switch (node.nodeType) {
				case Node.ELEMENT_NODE:
					return _getClassName(node) == MN_PH_CLASS;
				case Node.TEXT_NODE:
					return !immediate && node.parentNode
							&& _getClassName(node.parentNode) == MN_PH_CLASS;
				}
				return false;
			},

			/**
			 * @param {Node}
			 *            editSection A editible section to create the editable
			 *            section placeholder for
			 * @return {Node} An editable section placeholder element.
			 */
			createESPlaceholder : function(editSection) {
				var phHTML = this.getEditProperties(editSection).phMarkup
						|| "&nbsp;";
				var ph = $createElement("span");
				_setClassName(ph, ES_PH_CLASS);
				ph.innerHTML = phHTML;
				return ph;
			},

			/**
			 * Determines whether a dom node is (part of) an editable section
			 * placeholder.
			 * 
			 * @param {Node}
			 *            node A dom node to test
			 * 
			 * @param {Boolean}
			 *            immediate True to only test if node is a placeholder.
			 *            False to also test if the nodes descendants is a
			 *            placeholder.
			 * 
			 * @return {Boolean} True if node is (part of) a editable section
			 *         placeholder. False if it is not
			 */
			isESPlaceHolder : function(node, immediate) {

				while (node) {
					if (node.nodeType == Node.ELEMENT_NODE) {
						var clsName = _getClassName(node);
						if (clsName == ES_PH_CLASS)
							return true;
					}
					if (immediate)
						break;
					node = node.parentNode;
				}
				return false;
			},

			/**
			 * Create and adds a new edit section to the document. Sets the
			 * classname for the given editable section. The is required when
			 * adding new editable sections <em>after initialization</em> so
			 * dedit can track its changes. <br/> Adds placeholders if they are
			 * needed.
			 * 
			 * @param {Node}
			 *            esEle The editable section to register
			 * @param {String}
			 *            propertySetName The name of the property set to use.
			 *            Can be null/empty for default set.
			 * 
			 * @see de.doc.removeEditSection For removing a editable section.
			 */
			registerEditSection : function(esEle, propertySetName) {

				// Set class name as editable
				var clsName = _getClassName(esEle);
				_setClassName(esEle, (clsName ? clsName + " " : "")
						+ _ES_CLASS_PREFIX
						+ (propertySetName ? "-" + propertySetName : ""));

				// Add an editable section placeholder if it needs it
				if (!_doesContainTanglableDescendant(esEle))
					esEle.appendChild(this.createESPlaceholder(esEle));

				this.fireEvent("SectionAdded", esEle); // NB: Changes module
														// listens
			},

			/**
			 * Unregisters an editable section from the document. Sets the
			 * classname for the given editable section
			 * 
			 * @param {Node}
			 *            esEle The edit section to remove from the document.
			 */
			unregisterEditSection : function(esEle) {

				// Strip classname of editable section prefix
				var clsName = _getClassName(esEle);
				if (clsName)
					_setClassName(esEle, clsName.replace(new RegExp("^|\s"
							+ _ES_CLASS_PREFIX + "\S*$", "g"), ""))

				this.fireEvent("SectionRemoved", esEle); // NB: Changes
															// module listens
			}

		}; // End doc namespace

	})();
	// End Doc.js
	// Start Error.js

	var _ErrorMessages = { // TODO: multi-language support ?

		'1' : "Bad arguments"

	};

	/**
	 * Raises an error and ends execution (throws error)
	 * 
	 * @param {Number,
	 *            String} arg If string the error raises with contain the
	 *            message otherwise if number then the error will contain the
	 *            message for the error code
	 */
	function _error(arg) {
		throw new Error(typeof arg == "number" ? _ErrorMessages[arg] : arg);
	}
	// End Error.js

	// Start Events.js
	/**
	 * Adds an event handler to a DOM Event Source.
	 * 
	 * If you are planning to listen for keyboard strokes, you can use
	 * "keystroke." This uses keydown and/or keypress and provides an extra
	 * argument to your handler containing the normalized key identifier string.
	 * 
	 * @param {Object}
	 *            eventSource The Element or Window to add the event to.
	 * 
	 * @param {String}
	 *            eventName The name of the dom event. Must be lowercase and
	 *            omit the "on" prefix. For example "load" or "keypress".
	 * 
	 * @param {Function}
	 *            handler A function to handle the event being registered. When
	 *            called, the dom event will be passed as the first (and only)
	 *            argument.
	 */
	function _addHandler(sourceEle, eventName, handlerFunc) {

		debug.assert(sourceEle.nodeType != Node.TEXT_NODE
				&& sourceEle.nodeType != Node.COMMENT_NODE);
		debug.assert(eventName.indexOf('on') !== 0);

		// IE has trouble passing the window object around - it can be cloned
		// for no reason
		if (sourceEle.setInterval && sourceEle != window)
			sourceEle = window;

		switch (eventName) {

		case "keystroke":
			// Keystroke is a combo of keydown and key press - normalizes
			// key identifiers and passes them as extra arg to handler
			addEventHandler(sourceEle, "keydown", function(e) {
				var keyIndent = de.events.Keyboard.getKeyIdentifier(e, true);
				if (keyIndent)
					return handlerFunc(e, keyIndent);
			});
			addEventHandler(sourceEle, "keypress", function(e) {
				var keyIndent = de.events.Keyboard.getKeyIdentifier(e, false);
				if (keyIndent)
					return handlerFunc(e, keyIndent);
			});
			break;

		case "mousedown": // Keep mouse state updated
			addEventHandler(sourceEle, eventName, function(e) {
				return de.events.Mouse.sniffMouseDownEvent(e) ? handlerFunc(e)
						: true;
			});
			break;
		case "mouseup": // Keep mouse state updated
			addEventHandler(sourceEle, eventName, function(e) {
				return de.events.Mouse.sniffMouseUpEvent(e) ? handlerFunc(e)
						: true;
			});
			break;
		case "mousemove": // Keep mouse state updated
			addEventHandler(sourceEle, eventName, function(e) {
				return de.events.Mouse.sniffMouseMoveEvent(e) ? handlerFunc(e)
						: true;
			});
			break;

		default:
			addEventHandler(sourceEle, eventName, handlerFunc);
		}

		function addEventHandler(sourceEle, eventName, actualHandler) {

			// Keep track of wrapper handlers for removal

			// Create event wrapper map (Event type -> tuple[event-source,
			// wrapper handler])
			if (!handlerFunc.evWrappers)
				handlerFunc.evWrappers = {};

			// Ensure the map contains an entry for this event type
			if (!handlerFunc.evWrappers[eventName])
				handlerFunc.evWrappers[eventName] = [];

			// Store this event tuple
			handlerFunc.evWrappers[eventName].push([ sourceEle, forwardEvent ]);

			if (sourceEle.addEventListener)
				sourceEle.addEventListener(eventName, forwardEvent, false); // DOM
																			// Compliant

			else if (sourceEle.attachEvent)
				sourceEle.attachEvent("on" + eventName, forwardEvent); // IE

			// @DEBUG ON
			else
				debug
						.assert(false,
								"Unsupported browser: does not support addEventListener or attachEvent");
			// @DEBUG OFF

			/**
			 * Wraps an event handler so that the event handler gets a non null
			 * event and if the handler returns false the event is consumed.
			 * 
			 * @param {Event}
			 *            e provided by native event dispatcher
			 */
			function forwardEvent(e) {
				de.events.current = e = e || window.event;
				try {
					if (actualHandler(e) === false)
						return de.events.consume(e);
				} finally {
					de.events.current = 0;
				}
			}

		}

	}

	/**
	 * Removes an event handler from a DOM Event Source.
	 * 
	 * @param {Object}
	 *            eventSource The Element or Window to add the event to.
	 * 
	 * @param {String}
	 *            eventName The name of the dom event. Must be lowercase and
	 *            omit the "on" prefix. For example "load" or "keypress".
	 * 
	 * @param {Function}
	 *            handler The handler function to remove form the given event
	 *            source.
	 */
	function _removeHandler(sourceEle, eventName, handlerFunc) {
		debug.assert(eventName.indexOf('on') !== 0);

		// Keystoke is comprized of keydown and key press
		if (eventName == "keystroke") {
			_removeHandler(sourceEle, 'keydown', handlerFunc);
			_removeHandler(sourceEle, 'keypress', handlerFunc);
			return;
		}

		// Check if the handler has been wrapped for this event type / source
		if (handlerFunc.evWrappers && handlerFunc.evWrappers[eventName]) {

			// Look for wrapper handlers for this event type for this handler
			for ( var i in handlerFunc.evWrappers[eventName]) {

				var tuple = handlerFunc.evWrappers[eventName][i];

				// Was there a wrapped handler create for this handler on this
				// event type and source?
				if (tuple[0] == sourceEle) {

					// Remove tuple
					if (handlerFunc.evWrappers[eventName].length == 1)
						delete handlerFunc.evWrappers[eventName];
					else
						handlerFunc.evWrappers[eventName].splice(i, 1);

					// Assign handlerFunc to be the wrapped handler
					handlerFunc = tuple[1];

					break;
				}

			}

		}

		// Remove the handler from the event source
		if (sourceEle.removeEventListener)
			sourceEle.removeEventListener(eventName, handlerFunc, false); // DOM
																			// Compliant

		else if (sourceEle.detachEvent)
			sourceEle.detachEvent("on" + eventName, handlerFunc); // IE

		// @DEBUG ON
		else
			debug
					.assert(false,
							"Unsupported browser: does not support addEventListener or attachEvent");
		// @DEBUG OFF

	}

	$extend(de.events, {

		/**
		 * A pointer to the current event
		 */
		current : 0,

		addHandler : _addHandler,

		removeHandler : _removeHandler,

		/**
		 * @param {Event}
		 *            e An event
		 * 
		 * @return {Object} The x and y coordinates of the event relative to the
		 *         window.
		 */
		getXYInWindowFromEvent : function(e) {

			var targetX = 0, targetY = 0;

			if (e.clientX || e.clientX === 0) {
				targetX = e.clientX;
				targetY = e.clientY;
			} else if (e.pageX != null) {
				var scrollPos = _getDocumentScrollPos();
				targetX = e.pageX - scrollPos.left;
				targetY = e.pageY - scrollPos.top;
			}

			return {
				x : targetX,
				y : targetY
			};
		},

		/**
		 * 
		 * @param {Event}
		 *            e A dom event
		 * @return {Node} An element or text node which the event was targetted
		 *         at.
		 */
		getEventTarget : function(e) {
			return e.target || e.srcElement || document;
		},

		/**
		 * Consumes an event... stop is from propagating and prevents the
		 * default action.
		 * 
		 * @example myhandlers = function(e) { ... if (somecondition) return
		 *          de.events.consume(e); return true; }
		 * 
		 * @param {Event}
		 *            e A dom event.
		 * 
		 * @return {Boolean} False always.
		 */
		consume : function(e) {
			de.events.stopPropogation(e);
			de.events.preventDefault(e);
			return false;
		},

		/**
		 * Stops the event from propogating / bubbling.
		 * 
		 * @param {Event}
		 *            e A dom event.
		 */
		stopPropogation : function(e) {
			if (_engine == _Platform.TRIDENT)
				e.cancelBubble = true;
			else if (e.stopPropagation)
				e.stopPropagation();
		},

		/**
		 * Prevents the default behaviour from occuring on a given event.
		 * 
		 * @param {Event}
		 *            e An event object
		 */
		preventDefault : function(e) {
			if (_engine == _Platform.TRIDENT)
				e.returnValue = false;
			else if (e.preventDefault)
				e.preventDefault();
		}

	});
	// End Events.js
	// Start mouse.js
	(function() {

		var LEFT_BUTTON = "1", RIGHT_BUTTON = "2", toNormalizedIDMap, // Platform
																		// dependant
																		// map
																		// for
																		// transating
																		// to
																		// platform
																		// independant
																		// values
		draggingScrollBars = false,
		// The keys are button values in dom mouse event objects.
		// The values are booleans, where true means they are down
		mouseStateMap = {};

		$enqueueInit("events.Mouse", function() {

			// Ensure that the document always captures mouse events to track
			// mouse state
			// _addHandler(document, "mousedown", function() {}); //
			// DEPRECIATED: Selection will always have these events
			// _addHandler(document, "mouseup", function() {});

			// Setup mouse-button map for translating button/which values into a
			// normalized/agreed value for all platforms
			if (_browser == _Platform.IE) {
				toNormalizedIDMap = {
					"1" : LEFT_BUTTON,
					"2" : RIGHT_BUTTON
				};
			} else {// DOM Complient
				// DOM have screwed up their specification. There is no way to
				// tell
				// reliably whether a left click is down since their button
				// value
				toNormalizedIDMap = {
					"0" : LEFT_BUTTON,
					"2" : RIGHT_BUTTON
				};
			}

			mouseStateMap[LEFT_BUTTON] = mouseStateMap[RIGHT_BUTTON] = false;

		});

		/**
		 * @class A singleton that provides cross-browser/platform mouse-state
		 *        facilties.
		 * @author Brook Jesse Novak
		 */
		de.events.Mouse = {

			sniffMouseDownEvent : function(e) {

				// Update mouse button state
				var normalizedClickID = toNormalizedIDMap[e.button];

				if (normalizedClickID) {
					// Left click + ctrl = right click on macs
					if (_os == _Platform.MAC
							&& normalizedClickID == LEFT_BUTTON && e.ctrlKey)
						normalizedClickID = RIGHT_CLICK;
					mouseStateMap[normalizedClickID] = true;
				}

				// Filter out mouse events on the document when the mouse is on
				// the scroll bars
				if (isOnScrollBars(e)) {
					draggingScrollBars = this.isLeftDown();
					return false;
				}

				return true;
			},

			sniffMouseUpEvent : function(e) {

				// Update mouse button state
				var normalizedClickID = toNormalizedIDMap[e.button];

				if (normalizedClickID) {
					// Left click + ctrl = right click on macs. However- the
					// user may have depressed the ctrl key before the
					// key up event. Therefore, if this is a mac, and if this
					// button up event is a left button, then set both left and
					// right
					// states to no longer being down.
					if (_os == _Platform.MAC
							&& normalizedClickID == LEFT_BUTTON)
						// Sure this isn't perfect but there is no way to tell -
						// and its not the end of the world,
						// I don't think the use of two buttons at the same time
						// is very useful for text-editors anyway!
						this.clearDownStates();
					else
						mouseStateMap[normalizedClickID] = false;
				}

				draggingScrollBars = this.isLeftDown();

				// Filter out mouse events on the document when the mouse is on
				// the scroll bars
				return !isOnScrollBars(e);
			},

			sniffMouseMoveEvent : function(e) {
				// Filter out mouse move events within the document (including
				// the actual document)
				// if the user is scrolling.
				return !(this.isLeftDown() && draggingScrollBars);
			},

			/**
			 * @return {Boolean} True if the left mouse button is currently
			 *         down.
			 * 
			 * <br>
			 * <b>Note</b>: On a mac, if the user left-clicks while ctrl is
			 * down, this seen a right click gesture for their single button
			 * mouse design - but also does the same thing for mouses with
			 * multiple buttons.
			 * 
			 */
			isLeftDown : function() {
				return mouseStateMap[LEFT_BUTTON];
			},

			/**
			 * @return {Boolean} True if the right mouse button is currently
			 *         down.
			 */
			isRightDown : function() {
				return mouseStateMap[RIGHT_BUTTON];
			},

			/**
			 * Clears the button down states. Useful if mouse module is unable
			 * to capture/sniff mouse up events in a particulat environment ...
			 * this provides a way to manually clear mousedown states.
			 */
			clearDownStates : function() {
				mouseStateMap[LEFT_BUTTON] = false;
				mouseStateMap[RIGHT_BUTTON] = false;
			}

		}; // End mouse singleton

		/**
		 * Determines whether mouse event targetted at the document is over
		 * scroll bars
		 * 
		 * @param {Object}
		 *            e
		 * @return {Boolean} Evaluate true if mouse event was on scrollbars
		 */
		function isOnScrollBars(e) {

			var target = de.events.getEventTarget(e);

			if (target == window || target == document
					|| target == document.documentElement) {

				// Opera never raises events on document scrollbars
				if (_engine == _Platform.PRESTO)
					return;

				var pos = de.events.getXYInWindowFromEvent(e), viewportSize = _getViewPortSize();
				if (pos.x >= viewportSize.width)
					return 1;

				return pos.y >= viewportSize.height;
			}

		}

	})();
	// End mouse.js
	// Start FormatEnvironment.js
	/**
	 * Stores all format environment variables. These are used by the undoable
	 * "Format" action.
	 */
	var _formatEnvironment = {};

	/**
	 * Sets a specific format variable in the format environment. Can use to
	 * create or override-existing formatting profiles which are used by the
	 * "Format" action.
	 * 
	 * @param {String}
	 *            name The name of the format type. For example "bold" (case
	 *            insensitive)
	 * @param {Function}
	 *            evalFunc The evaluation function. TODO: DOCUMENT
	 * @param {Function}
	 *            wrapperFunc The wrapper function. Must return an inline
	 *            element. TODO DOCUMENT
	 */
	function _setFormatEnvVar(name, evalFunc, wrapperFunc) {
		name = name.toLowerCase();
		_formatEnvironment[name + "Eval"] = evalFunc;
		_formatEnvironment[name + "Wrapper"] = wrapperFunc;
	}
	;

	// Create the default set of formatting variables
	(function() {

		/**
		 * Creates a span with a CSS style
		 * 
		 * @param {String}
		 *            css The CSS style name
		 * @param {String}
		 *            val The style value to set
		 * @return {Node} The created wrapper
		 */
		function createCSSSpan(css, val) {
			var wrapper = $createElement("span");
			_setStyle(wrapper, css, val);
			return wrapper;
		}

		(function(envVars) {
			for ( var i in envVars) {
				_setFormatEnvVar(i, envVars[i][0], envVars[i][1]);
			}

		})({

			bold : [
			/* Evaluation function */
			function(ele) {

				var matches = [];

				if (_nodeName(ele) == "b" || _nodeName(ele) == "strong")
					matches.push({
						type : 1
					});

				var fw = ele.style.fontWeight;
				if (fw) {
					var isBold = fw == "bold";
					if (!isBold) {
						fw = parseInt(fw);
						isBold = (!isNaN(fw) && fw >= 700)
					}
					if (isBold)
						matches.push({
							type : 3,
							match : "fontWeight"
						});
				}

				return matches.length > 0 ? {
					strip : matches,
					inline : $createElement("strong"), // Extracted inline
														// equivalent
					value : true
				} : null;

			},

			/* Wrapper */
			function() {
				return $createElement("strong");
			} ],

			italics : [
			/* Evaluation function */
			function(ele) {

				var matches = [];

				if (_nodeName(ele) == "i" || _nodeName(ele) == "em")
					matches.push({
						type : 1
					});

				if (ele.style.fontStyle == "italic")
					matches.push({
						type : 3,
						match : "fontStyle"
					});

				return matches.length > 0 ? {
					strip : matches,
					inline : $createElement("em"), // Extracted inline
													// equivalent
					value : true
				} : null;
			},

			/* Wrapper */
			function() {
				return $createElement("em");
			}

			],

			underline : [
			/* Evaluation function */
			function(ele) {

				var matches = [];

				if (_nodeName(ele) == "u")
					matches.push({
						type : 1
					});

				if (ele.style.textDecoration == "underline")
					matches.push({
						type : 3,
						match : "textDecoration"
					});

				return matches.length > 0 ? {
					strip : matches,
					inline : createCSSSpan("textDecoration", "underline"), // Extracted
																			// inline
																			// equivalent
					value : true
				} : null;
			},

			/* Wrapper */
			function() {
				return createCSSSpan("textDecoration", "underline");
			}

			],

			strike : [
			/* Evaluation function */
			function(ele) {

				var matches = [];

				if (_nodeName(ele) == "strike")
					matches.push({
						type : 1
					});

				if (ele.style.textDecoration == "line-through")
					matches.push({
						type : 3,
						match : "textDecoration"
					});

				return matches.length > 0 ? {
					strip : matches,
					inline : createCSSSpan("textDecoration", "line-through"), // Extracted
																				// inline
																				// equivalent
					value : true
				} : null;
			},

			/* Wrapper */
			function() {
				return createCSSSpan("textDecoration", "line-through");
			}

			],

			color : [

			/* Evaluation function */
			function(ele) {

				if (ele.style.color && ele.style.color.length > 0)
					return {
						strip : [ {
							type : 3,
							match : "color"
						} ],
						inline : createCSSSpan("color", ele.style.color),
						value : ele.style.color
					};

			},

			/* Wrapper */
			function(value) {
				return createCSSSpan("color", value);
			}

			],

			backcolor : [

					/* Evaluation function */
					function(ele) {

						if (ele.style.backgroundColor
								&& ele.style.backgroundColor.length > 0)
							return {
								strip : [ {
									type : 3,
									match : "backgroundColor"
								} ],
								inline : createCSSSpan("backgroundColor",
										ele.style.backgroundColor),
								value : ele.style.backgroundColor
							};

					},

					/* Wrapper */
					function(value) {
						return createCSSSpan("backgroundColor", value);
					}

			],

			fontsize : [
			/* Evaluation function */
			function(ele) {

				if (_nodeName(ele) == "small" || _nodeName(ele) == "big") {
					var val = _nodeName(ele) == "small" ? "smaller" : "larger";
					return {
						strip : [ {
							type : 1
						} ],
						inline : createCSSSpan("fontSize", val),
						value : val
					};
				}

				if (ele.style.fontSize && ele.style.fontSize.length > 0)
					return {
						strip : [ {
							type : 3,
							match : "fontSize"
						} ],
						inline : createCSSSpan("fontSize", ele.style.fontSize),
						value : ele.style.fontSize
					};
			},

			/* Wrapper */
			function(value) {
				return createCSSSpan("fontSize", value);
			} ],

			fontfamily : [
					/* Evaluation function */
					function(ele) {
						if (ele.style.fontFamily
								&& ele.style.fontFamily.length > 0)
							return {
								strip : [ {
									type : 3,
									match : "fontFamily"
								} ],
								inline : createCSSSpan("fontFamily",
										ele.style.fontFamily),
								value : ele.style.fontFamily
							};
					},

					/* Wrapper */
					function(value) {
						return createCSSSpan("fontFamily", value);
					} ],

			link : [

			/* Evaluation function */
			function(ele) {
				if (_nodeName(ele) == "a")
					return {
						strip : [ {
							type : 1
						} ],
						inline : ele.cloneNode(false),
						value : {
							url : ele.href,
							title : ele.title
						}
					};
			},

			/* Wrapper */
			function(value) {
				var wrapper = $createElement("a");
				wrapper.href = value.url;
				wrapper.title = value.title;
				return wrapper;
			} ]
		});

	})();
	// End FormatEnvironment.js
	// Start Fragment.js
	/**
	 * @class
	 * 
	 * DOMFragment's are used to describe a range of DOM nodes. They are similar
	 * W3C DOM Ranges, except they have a more rich/expressive range and contain
	 * information for reversing operations safely.
	 * 
	 * @constructor
	 * @private
	 * 
	 * Use de.DOMFragment.buildFragment
	 * 
	 * @param {Node}
	 *            domNode The dom node to wrap
	 * @param {Number}
	 *            posInParent The position of the domNode in is parent child
	 *            list.
	 * 
	 */
	var _DOMFragment = function() {

		var cls = function(domNode, posInParent) {

			/**
			 * The wrapped dom node
			 * 
			 * @type Node
			 */
			this.node = domNode;

			/**
			 * A read only member: The position of the domNode in is parent
			 * child list.
			 * 
			 * @type Number
			 */
			this.pos = posInParent;

			/**
			 * A read only member: The de.dom.DOMFragment's children. Never
			 * null.
			 * 
			 * @type [de.dom.DOMFragment]
			 */
			this.children = [];

			/**
			 * True indicates that this node has descendants (including self)
			 * that are outside of the fragment range. Flase indicates that the
			 * node is completely within the fragments range.
			 * 
			 * @type Boolean
			 */
			this.isShared = false;

			/**
			 * The dom fragments parent. Null for root.
			 * 
			 * @type de.dom.DOMFragment
			 */
			this.parent = null;
		}

		cls.prototype = {

			/**
			 * Applies a function to nodes in order starting from this node and
			 * all its children.
			 * 
			 * @param {Function}
			 *            func A function applied to the nodes. 1 argument is
			 *            given: a visited node. The traversing will be stopped
			 *            by returning false.
			 */
			visit : function(func) {

				if (func(this) === false)
					return false;

				for ( var i in this.children) {
					if (!this.children[i].visit(func))
						return false;
				}

				return true;
			},

			/**
			 * 
			 * Removes the fragment from the document.
			 * 
			 */
			disconnect : function() {
				this.visit(function(currentFrag) {
					if (!currentFrag.isShared)
						removeFragment(currentFrag, false);
				});
			},

			/**
			 * An extension of de.dom.DOMFragment#disconnect. This routine
			 * furthermore collapses nodes after the disconnection: where nodes
			 * the have been left in the document -- within the fragment range --
			 * are removed/copied/moved in a way that a typical word-proccessor
			 * would behave. <br>
			 * <br>
			 * The fragment must not be disconnected prior to this call.
			 * 
			 * @return {Node} The first migrated node. Null if nothing was
			 *         migrated.
			 */
			collapse : function() {

				// Get the first common ancestor between the start and end
				// fragments... this may not be the root.
				var firstCommonAncestor = _getCommonAncestor(this
						.getStartFragment().node, this.getEndFragment().node,
						false), fcaFrag = this, stopMigratingSiblings = false, // used
																				// for
																				// inner
																				// function
				fragRoot = this, // used for inner function
				hasCollapsibleBoundAncestor = false; // used for inner
														// function

				while (fcaFrag.node != firstCommonAncestor) {
					fcaFrag = fcaFrag.children[0];
				}

				// Remove the fragments inclusive range from the document
				fragRoot.disconnect();

				// Insert any placeholders that needs placing
				insertPlaceholders();

				// Remove all migratable nodes and build migration trees
				var migrations = migratePath(fcaFrag, true,
						nextMigrationPoint(fragRoot.getStartFragment())), i, j;

				// Add migration trees into their migration points
				for (i in migrations) {

					if (migrations[i].migrantRoots.length > 0) {

						// Discover the insertion index in the migration point
						var mPointNode = migrations[i].migrationPoint.node, insertIndex = migrations[i].migrationPoint.children[0].pos, // Start-bound's
																																		// index
						containsNonPHTangable = false, seenPlaceholder = 0;

						// If the start-bound child is still in the document,
						// then increase the index
						if (isInDocument(migrations[i].migrationPoint.children[0].node))
							insertIndex++;

						// Insert the migrants
						for (j = 0; j < migrations[i].migrantRoots.length; j++) {
							_execOp(_Operation.INSERT_NODE,
									migrations[i].migrantRoots[j], mPointNode,
									insertIndex + j);
						}

						// Check to see if migration point has any redundant
						// placeholders, which may have been uneccessarely
						// added,
						// or just moved, via the collapse operation.

						// First check if there are any tangable descendants of
						// the migration point which are not
						// immediate children that are placeholders...
						if (_isPlaceholderCandidate(mPointNode)) {
							_visitAllNodes(
									mPointNode,
									mPointNode,
									true,
									function(node) {
										if (node == mPointNode)
											return;
										if (!(de.doc.isMNPlaceHolder(node,
												false) && (node.nodeType == Node.TEXT_NODE ? node.parentNode.parentNode == mPointNode
												: node.parentNode == mPointNode))
												&& de.cursor
														.getPlacementFlags(node)) {
											containsNonPHTangable = true;
											return false;
										}

									});
						}

						// Scan through the migration points immediate nodes
						for (var k = 0; k < mPointNode.childNodes.length; k++) {

							var domNode = mPointNode.childNodes[k];

							if (de.doc.isMNPlaceHolder(domNode, true)
									|| de.doc.isESPlaceHolder(domNode, true)) {

								// If we have already seen a placeholder in this
								// migration point, or the migration point is
								// not
								// a placeholder candidate, or the migration
								// point contains tangable nodes that are not
								// immediate
								// children who are placeholders... then this
								// placeholder is redundant
								if (seenPlaceholder
										|| !_isPlaceholderCandidate(mPointNode)
										|| containsNonPHTangable)
									_execOp(_Operation.REMOVE_NODE, domNode); // Remove
																				// it
																				// from
																				// the
																				// document

								seenPlaceholder = 1;
							}
						}
					}
				}

				// Return the first migrated node
				return migrations.length > 0
						&& migrations[0].migrantRoots.length > 0 ? migrations[0].migrantRoots[0]
						: null;

				/**
				 * Removes/clones nodes from the document and creates migration
				 * trees -- paired with their migration points, for which they
				 * should be appended to.
				 * 
				 * @param {de.dom.DOMFragment}
				 *            pathRoot The top-most fragment of the path to
				 *            travel up to.
				 * @param {Object}
				 *            isEndBoundPath True iff the path is the
				 *            end-boundry path
				 * @param {de.dom.DOMFragment}
				 *            curMigPoint The current migration point
				 * @return {[Object]} The migrations to make
				 */
				function migratePath(pathRoot, isEndBoundPath, curMigPoint) {

					var curMigration = {
						/*
						 * A migration point is a dom node in the start-bound
						 * path where nodes in the end bound path can
						 * move(migrate) to.
						 */
						migrationPoint : curMigPoint,

						/*
						 * Migration roots are disconnected dom-trees which
						 * should be connected with the migration point in this
						 * instance by adding as children ... which is done
						 * outside of this function scope.
						 */
						migrantRoots : []
					}

					var migrations = [ curMigration ];

					// Follow a path starting from the end node and moving up to
					// the path's root
					for (var fragment = isEndBoundPath ? pathRoot
							.getEndFragment() : pathRoot.getStartFragment(); fragment != (isEndBoundPath ? pathRoot
							: pathRoot.parent); fragment = fragment.parent) {

						var domNode, nextNode;

						// Check to see if this fragment has an ancestor who is
						// collapsible and is on the end-bound path.
						if (isEndBoundPath) {
							hasCollapsibleBoundAncestor = false;
							for (var f = fragment.parent; f != pathRoot; f = f.parent) {
								if (f.isShared && isCollapsible(f.node)) {
									hasCollapsibleBoundAncestor = true;
									break;
								}
							}
						}

						if (!isEndBoundPath || fragment.isShared) {

							domNode = fragment.node;
							nextNode = domNode.nextSibling;

							// Should this node be removed? I.E: Is the node
							// collapsable?
							if (isCollapsible(domNode)) {

								stopMigratingSiblings = true;

								// All descendants of a collapsiable node on the
								// bound path should be migrated
								debug
										.assert(!isEndBoundPath
												|| (isEndBoundPath && !domNode.firstChild));

								// Remove this collapsible node if it is on the
								// end-bound path and is shared
								if (isEndBoundPath)
									_execOp(_Operation.REMOVE_NODE,
											fragment.node);

							}

							// Check to see if this node should be migrated, or
							// should stay
							if (!isEndBoundPath || canDirectlyMigrate(domNode)) {

								// Check if need to remove this migrant or
								// duplicate it
								if (domNode.firstChild)
									domNode = domNode.cloneNode(false);
								else
									_execOp(_Operation.REMOVE_NODE, domNode);

								// Is the current migration point valid?
								while (curMigration.migrationPoint != fcaFrag
										&& !_isValidRelationship(
												domNode,
												curMigration.migrationPoint.node)
										&& _nodeName(domNode) != "div") {
									// One exception: don't allow migration of
									// div element: the generic container

									// Search for next valid migration point...
									curMigration.migrationPoint = nextMigrationPoint(curMigration.migrationPoint);
								}

								// Add this node to all migrations
								for (var i = 0; i < migrations.length; i++) {

									// Link up dom node to its children
									for ( var j in migrations[i].migrantRoots) {
										_execOp(_Operation.INSERT_NODE,
												migrations[i].migrantRoots[j],
												domNode);
									}

									// Set the new roots for this tree-level
									migrations[i].migrantRoots = [ domNode ];

									// Duplicate node for next migration point
									if (i < (migrations.length - 1))
										domNode = domNode.cloneNode(false);

								}

							} else if (!isCollapsible(domNode)) { // Fragment
																	// in
																	// boundry
																	// path that
																	// cannot be
																	// migrated

								stopMigratingSiblings = true;
								if (!domNode.firstChild)
									// The un-migratible node has been left
									// childless, remove it from the document
									_execOp(_Operation.REMOVE_NODE,
											fragment.node);
							}

						} else { // Is on end bound path and fragment not
									// shared and has been disconnected. i.e.
									// completely removed from document

							// Determine next node...

							// If the parent has been disconnected, then there
							// is no use searching through the next siblings
							if (!fragment.parent.isShared)
								continue;

							// See if this fragments parent is part of the
							// starting bound
							var startBound = fragRoot;
							while (startBound.children.length > 0
									&& startBound != fragment.parent) {
								startBound = startBound.children[0];
							}

							if (startBound == fragment.parent) {
								debug.assert(startBound.children.length > 0);

								// If the start bound path shares the same
								// parent for the current fragment
								// (which is on the end path), then the next
								// sibling is not neccesarily the first
								// remaining child in this fragment's parent.
								nextNode = null;
								if (!startBound.insertedPH) { // Watch out for
																// these, since
																// they are
																// added on the
																// fly, not in
																// fragment
																// structure

									if (isInDocument(startBound.children[0].node)) {
										// The first child of the start bound is
										// in the document
										nextNode = startBound.node.childNodes.length > (startBound.children[0].pos + 1) ? startBound.node.childNodes[startBound.children[0].pos + 1]
												: null;
									} else {
										// The first child of the start bound
										// has been removed
										nextNode = startBound.node.childNodes.length > startBound.children[0].pos ? startBound.node.childNodes[startBound.children[0].pos]
												: null;
									}

								}

								// Get the first remainding child in this
								// disconnected fragment's parent
							} else
								nextNode = fragment.parent.node.firstChild; // May
																			// be
																			// null/not
																			// exist

						}

						// If the parent cannot be migrated from the end-bounds
						// path, then don't migrate it's children to
						// the left of the end-bounds.
						if (isEndBoundPath
								&& !canDirectlyMigrate(fragment.parent.node)
								&& !isCollapsible(fragment.parent.node))
							stopMigratingSiblings = true;

						// If this fragment is for the starting iteration of a
						// new migrant fragment, then avoid recursing into
						// it's sibling dom nodes that are not in a fragment
						// yet... this will be handled afterwards
						else if (!isEndBoundPath && fragment == pathRoot)
							continue;

						// Get this fragments child-index in it's parent
						var nextFragIndex = fragment.getIndexInParent();

						// Set/reset the migration index for this fragments
						// parent. Only applicable on the bound path
						if (isEndBoundPath)
							fragment.parent.migrantIndex = 0;

						while (nextNode
								&& (hasCollapsibleBoundAncestor || !stopMigratingSiblings)) { // For
																								// each
																								// adjacent
																								// path

							domNode = nextNode; // For bound path, valid for
												// non-bound path
							nextNode = domNode.nextSibling; // For bound path,
															// valid for
															// non-bound path
							nextFragIndex++; // For non-bound path

							var adjacentMigrations;

							if (isEndBoundPath) {

								var migrantFragRoot;

								// If this operation is being repeated, then get
								// the migration tree created from the first
								// time
								// this operation was performed
								if (fragment.parent.migrants
										&& fragment.parent.migrants.length > fragment.parent.migrantIndex) {

									migrantFragRoot = fragment.parent.migrants[fragment.parent.migrantIndex];

								} else { // First time this operation has
											// been performed

									// Build a fragment to capture the structure
									// of the current document's state
									migrantFragRoot = _buildFragment(
											domNode.parentNode, domNode, 0,
											domNode, _nodeLength(domNode, 1));

									// Link it to the parent fragment
									if (!fragment.parent.migrants)
										fragment.parent.migrants = [];
									fragment.parent.migrants
											.push(migrantFragRoot);
								}

								fragment.parent.migrantIndex++;

								adjacentMigrations = migratePath(
										migrantFragRoot.children[0],
										false,
										migrations[migrations.length - 1].migrationPoint); // Recurse

							} else { // Fragment already created...

								adjacentMigrations = migratePath(
										fragment.parent.children[nextFragIndex],
										false,
										migrations[migrations.length - 1].migrationPoint); // Recurse
							}

							// Merge the adjacent migrations into the current
							// migrations
							for ( var i in adjacentMigrations) {
								if (adjacentMigrations[i].migrantRoots.length == 0)
									continue;

								// Search for a matching migration point
								var wasMerged = false;
								for ( var j in migrations) {

									if (migrations[j].migrationPoint == adjacentMigrations[i].migrationPoint) {
										// Append the adjacent migrant roots for
										// this migration point (at the current
										// tree-level)
										migrations[j].migrantRoots = migrations[j].migrantRoots
												.concat(adjacentMigrations[i].migrantRoots);
										wasMerged = true;
										break;
									}
								}

								// Must be a new migration point... add to the
								// current set of migration points
								if (!wasMerged)
									migrations.push(adjacentMigrations[i]);

							}

							// Keep the current migration updated
							curMigration = migrations[migrations.length - 1]; // TODO:
																				// NEEDED?

						} // End loop: recursing over adjacent paths

					} // End loop: travelling up path to root

					return migrations;
				} // End inner migratePath

				/**
				 * Looks up the start-bound path from the given migration point.
				 * 
				 * @param {_Fragment}
				 *            currentPoint Must not be the first Common Ancestor -
				 *            must be on the start bound path.
				 * @return {_Fragment} The next migration point
				 */
				function nextMigrationPoint(currentPoint) {

					debug.assert(currentPoint != fcaFrag);

					do {

						currentPoint = currentPoint.parent;

					} while (currentPoint != fcaFrag && !(

					// currentPoint.isShared && // NOTE: Cannot used isShared to
					// determine if exists in document... since cna be
					// re-inserted
					isInDocument(currentPoint.node) && /*
														 * i.e: shared nodes or
														 * re-inserted nodes due
														 * to placeholders
														 */
					isCollapsible(currentPoint.node)));

					return currentPoint;
				} // End inner nextMigrationPoint

				/**
				 * @param {Node}
				 *            domNode
				 * @return {Boolean} True if domNode is classed as "collapsible"
				 */
				function isCollapsible(domNode) {
					return _isGenericBlockLevel(domNode)
							|| _nodeName(domNode) == "li";
				} // End inner isCollapsible

				/**
				 * @param {Node}
				 *            domNode
				 * @return {Boolean} True if domNode can directly be migrated
				 *         from the boundry path into a migration point
				 */
				function canDirectlyMigrate(domNode) {
					return domNode.nodeType == Node.TEXT_NODE
							|| _isInlineLevel(domNode);
				} // End inner canDirectlyMigrate

				function isInDocument(domNode) {
					return _isAncestor(docBody, domNode);
				}

				/**
				 * If the start-bound path contains any placeholder candidates
				 * either left without tangable nodes or are fully disconnected,
				 * then placeholders are inserted / fragments are reconnected
				 */
				function insertPlaceholders() {

					for (var fragment = fragRoot.getStartFragment(); fragment != null; fragment = fragment.parent ? fragment.parent
							: null) {

						var domRef = fragment.node;

						if (_isPlaceholderCandidate(domRef)
								|| de.doc.isEditSection(domRef)) {

							// Is this still in the document?
							if (fragment.isShared) {

								var phType = 0
								if (_doesNeedESPlaceholder(domRef))
									phType = 1;
								else if (_doesNeedMNPlaceholder(domRef))
									phType = 2;

								if (phType) {

									// The disconnection has left a placeholder
									// candidate in the document without a
									// tangable node.

									// Create a new placeholder and add it
									var ph = phType == 1 ? de.doc
											.createESPlaceholder(domRef)
											: de.doc.createMNPlaceholder();

									_execOp(_Operation.INSERT_NODE, ph, domRef);

									// Mark that this fragment inserted a
									// placeholder
									fragment.insertedPH = 1;
								}

							} else { // disconnected

								// Get rid of any children that were part of the
								// diconnection
								while (domRef.firstChild) {
									_execOp(_Operation.REMOVE_NODE,
											domRef.firstChild);
								}

								// Create a new placeholder and add it
								_execOp(_Operation.INSERT_NODE, de.doc
										.createMNPlaceholder(), domRef);

								// Mark that this fragment inserted a
								// placeholder
								fragment.insertedPH = 1;

								// Link this and all it's disconnect ancestors
								// back into the document...
								while (!fragment.isShared) {

									if (fragment.parent.isShared) {

										// Reconnect this with it's parent
										_execOp(_Operation.INSERT_NODE,
												fragment.node,
												fragment.parent.node,
												fragment.pos);

									} else {

										// Make sure the parent has all its
										// children removed
										var parentDomNode = fragment.parent.node;
										while (parentDomNode.firstChild) {
											_execOp(_Operation.REMOVE_NODE,
													parentDomNode.firstChild);
										}

										// Reconnect this with it's parent
										_execOp(_Operation.INSERT_NODE,
												fragment.node, parentDomNode);

									}

									if (!fragment.parent)
										break;
									fragment = fragment.parent;
								}

							}

							break; // No need to search ancestors for inserting
									// placeholders as this point

						}
					}
				}

			},

			/**
			 * @return {de.dom.DOMFragment} The start fragment of the fragment's
			 *         range.
			 */
			getStartFragment : function() {
				var startFrag = this;
				while (startFrag.children.length > 0) {
					startFrag = startFrag.children[0];
				}
				return startFrag;
			},

			/**
			 * @return {de.dom.DOMFragment} The end fragment of the fragment's
			 *         range.
			 */
			getEndFragment : function() {
				var endFrag = this;
				while (endFrag.children.length > 0) {
					endFrag = endFrag.children[endFrag.children.length - 1];
				}
				return endFrag;
			},

			/**
			 * @return {Integer} The zero-based index of this fragment in it's
			 *         parent. Null if this is a root fragment.
			 */
			getIndexInParent : function() {

				if (!this.parent)
					return null;

				var index = 0;
				while (this != this.parent.children[index]) {
					index++;
				}
				return index;
			},

			/**
			 * @return {Boolean} True if the start node was a split text node,
			 *         so that it has a preceeding text node which was the
			 *         orginal node, false otherwise.
			 */
			wasStartSplit : function() {
				return this.getStartFragment().preSplitNode ? true : false;
			},
			/**
			 * @return {Boolean} True if the end node was a split text node, so
			 *         that it has a proceeding text node which was the new
			 *         split node, false otherwise.
			 */
			wasEndSplit : function() {
				return this.getEndFragment().postSplitNode ? true : false;
			},

			/**
			 * @return {Node} The previous text node which will split at the
			 *         start fragment. Null if this framgent didn't split the
			 *         start point
			 */
			getPreSplitNode : function() {
				var startFrag = this.getStartFragment();
				return startFrag.preSplitNode ? startFrag.preSplitNode : null;
			},

			/**
			 * @return {Node} The following text node which will split at the
			 *         end fragment. Null if this framgent didn't split the end
			 *         point
			 */
			getPostSplitNode : function() {
				var endFrag = this.getEndFragment();
				return endFrag.postSplitNode ? endFrag.postSplitNode : null;
			},

			/**
			 * Gets the adjusted node/index of the given tuple to point to a
			 * valid position in the DOM which they may have been left pointing
			 * to invalid indexes due to the construction of this fragment.
			 * 
			 * @param {Object}
			 *            node
			 * 
			 * @param {Object}
			 *            index
			 * 
			 */
			getAdjustedNodeIndex : function(node, index) {

				// Check if node belongs in the formatting fragments' split text
				// nodes, and see if it's index is out of bounds
				if (node.nodeType == Node.TEXT_NODE
						&& index >= _nodeLength(node)) {

					var startFrag = this.getStartFragment(), endFrag = this
							.getEndFragment();

					// Was the start node split? And did the node previously
					// point to this split node?
					if (startFrag.wasStartSplit()) {
						var preSplitNode = startFrag.getPreSplitNode();
						if (node == preSplitNode) {
							index -= _nodeLength(preSplitNode);
							node = startFrag.node;
						}
					}

					// Was the end node split? And did the node previously point
					// to the split node?
					if (endFrag.wasEndSplit() && node == endFrag.node
							&& index >= _nodeLength(node)) {
						index -= _nodeLength(endFrag.node);
						node = endFrag.getPostSplitNode();
					}
				}

				return {
					node : node,
					index : index
				};

			},

			/**
			 * Gets the original node/index values before this fragment was
			 * built.
			 * 
			 * @param {Object}
			 *            node
			 * @param {Object}
			 *            index
			 */
			getOriginalNodeIndex : function(node, index) {

				if (node.nodeType == Node.TEXT_NODE) {

					var startFrag = this.getStartFragment(), endFrag = this
							.getEndFragment();

					// Was the end node split? And does the node point to the
					// added split node?
					if (node == endFrag.getPostSplitNode()) {
						index += _nodeLength(endFrag.node);
						node = endFrag.node;
					}

					// Was the start node split? And does the node point to the
					// added split node?
					if (startFrag.wasStartSplit() && node == startFrag.node) {
						var preSplitNode = startFrag.getPreSplitNode();
						index += _nodeLength(preSplitNode);
						node = preSplitNode;
					}
				}

				return {
					node : node,
					index : index
				};
			}

		};

		// @DEBUG ON
		// Add tostring method for debugging
		cls.prototype.toString = function() {
			return _nodeName(this.node)
					+ (this.isShared ? "[SHARED]" : "[NOT-SHARED]");
		}
		// @DEBUG OFF

		/**
		 * Removes the fragments dom node from the document.
		 * 
		 * @param {de.dom.DOMFragment}
		 *            fragment
		 * @param {Boolean}
		 *            forceRemoveDoc Set to true to always remove the fragments
		 *            node from the document. Set to false to only remove if the
		 *            fragment has a shared parent.
		 */
		function removeFragment(fragment, forceRemoveDoc) {
			if (forceRemoveDoc || (fragment.parent && fragment.parent.isShared))
				_execOp(_Operation.REMOVE_NODE, fragment.node);
		}

		return cls;

	}();

	/**
	 * Builds a fragment. <br>
	 * <br>
	 * If startNode / endNode are the same, then startIndex / endIndex must be
	 * different. If startNode or endNodes are placeholders, they are extended
	 * to include the whole placeholder in the range. <br>
	 * The start node/index tuple must occur before the end node/index tuple
	 * when traversing inorder. <br>
	 * The range will always been extended to the deepest descendants.
	 * 
	 * @param {Node}
	 *            commonAncestor (Optional) A common ancestor of the start and
	 *            end nodes, can be as high as possible. If null then the
	 *            closest common ancestor will be used.
	 * 
	 * @param {Node}
	 *            startNode The starting dom node of the fragments range.
	 * 
	 * @param {Number}
	 *            startIndex The inclusive start index in the start node. Ranges
	 *            from 0 to the text length for text nodes. Where 0 indicates
	 *            that the range begins at the first char, and text length
	 *            indicates that the range begins directly after the text node,
	 *            but not including it. <br>
	 *            Ranges from 0 to 1 for elements. Where 0 indicates that the
	 *            range includes the element and its decendants, and 1 indicates
	 *            that the range excludes the element and its decendants.
	 * 
	 * 
	 * @param {Node}
	 *            endNode The ending dom node of the fragments range.
	 * 
	 * @param {Number}
	 *            endIndex The inclusive end index in the end node. Ranges from
	 *            0 to the text length for text nodes. Where 0 indicates that
	 *            the range ends just before the text node, but not including
	 *            it, and text length indicates that the range ends at the last
	 *            charactor in the text run. <br>
	 *            Ranges from 0 to 1 for elements. Where 0 indicates that the
	 *            range excludes the element and its decendants, and 1 indicates
	 *            that the range includes the element and its decendants.
	 * 
	 * @return {de.dom.DOMFragment} The fragments root FragmentNode, which will
	 *         always be the commonAncestor. Never null.
	 */
	function _buildFragment(commonAncestor, startNode, startIndex, endNode,
			endIndex) {

		debug.assert(startNode != endNode || startIndex < endIndex,
				"Invalid range");

		debug
				.assert(
						!(startNode.nodeType == Node.TEXT_NODE && (startIndex < 0 || startIndex > _nodeLength(startNode))),
						"Start index out of range");

		debug
				.assert(
						!(startNode.nodeType == Node.ELEMENT_NODE && (startIndex < 0 || startIndex > 1)),
						"Start index out of range");

		debug
				.assert(
						!(endNode.nodeType == Node.TEXT_NODE && (endIndex < 0 || endIndex > _nodeLength(endNode))),
						"End index out of range");

		debug
				.assert(
						!(endNode.nodeType == Node.ELEMENT_NODE && (endIndex < 0 || endIndex > 1)),
						"End index out of range");

		debug.assert(!(endNode == startNode && endIndex == startIndex),
				"Invalid range");

		// Set common ancestor if not given
		if (!commonAncestor)
			commonAncestor = _getCommonAncestor(startNode, endNode);

		// Make sure the range extends to the deepest descendants
		var adjustBoundry = false;
		while (startNode.firstChild) {
			startNode = startIndex == 0 ? startNode.firstChild
					: startNode.lastChild;
			adjustBoundry = true;
		}
		if (adjustBoundry && startIndex > 0)
			startIndex = _nodeLength(startNode, 1);

		adjustBoundry = false;
		while (endNode.firstChild) {
			endNode = endIndex == 0 ? endNode.firstChild : endNode.lastChild;
			adjustBoundry = true;
		}
		if (adjustBoundry && endIndex > 0)
			endIndex = _nodeLength(endNode, 1);

		// Make sure placeholders are fully included within the inclusive range
		if (de.doc.isMNPlaceHolder(startNode)
				|| de.doc.isESPlaceHolder(startNode))
			startIndex = 0;

		if (de.doc.isMNPlaceHolder(endNode) || de.doc.isESPlaceHolder(endNode))
			endIndex = _nodeLength(endNode, 1);

		// @DEBUG ON
		// Check that end occurs after the start
		var foundEnd = false;
		_visitAllNodes(commonAncestor, startNode, true, function(domNode) {
			foundEnd = domNode == endNode;
			return !foundEnd;
		});
		debug.assert(foundEnd,
				"Invalid range: end node not found after start node");
		// @DEBUG OFF

		var affectedNodes = [];

		// Get all the nodes between the start and end nodes.
		_visitAllNodes(commonAncestor, startNode, true, function(node) {
			affectedNodes.push(node);
			return node != endNode;
		});

		// Add the missing nodes to the start of the array
		var ancestors = _getAncestors(startNode, commonAncestor, false, true);
		ancestors.reverse();
		affectedNodes = ancestors.concat(affectedNodes);

		// to traverse the nodes inorder from the common ancestor through
		// the start and end nodes range, just iterate forwards on the array:
		var fragRoot;

		for ( var i in affectedNodes) {

			// Build up a fragment by traversing through the affected nodes in
			// order
			var node = affectedNodes[i], preSplitNode = 0, postSplitNode = 0;

			// Perform split operations on boundry text nodes
			if ((node == startNode || node == endNode)
					&& node.nodeType == Node.TEXT_NODE) {

				// Because the first/last nodes are text nodes, we may have to
				// split them up.
				if (node == startNode && startIndex > 0
						&& startIndex < _nodeLength(node)) {

					preSplitNode = node;

					// Leave the start node's stable references as is, since the
					// remainding text
					// node within the range is a newly created text node...
					node = _execOp(_Operation.SPLIT_TEXT_NODE, node, startIndex);

					// Update endnode if range is all within same node
					if (endNode == startNode) {
						endNode = node;
						endIndex -= _nodeLength(startNode);
					}

					startNode = node;
					// No need to worry about start index
				}

				if (node == endNode && endIndex > 0
						&& endIndex < _nodeLength(node)) {
					postSplitNode = _execOp(_Operation.SPLIT_TEXT_NODE, node,
							endIndex);
				}

			}

			// Create a new fragment node
			var fragment = new _DOMFragment(node, _indexInParent(node));

			// Has the root been set?
			if (!fragRoot)
				fragRoot = fragment;
			else { // Set up the parent relationship...

				fragment.parent = null;
				fragRoot.visit(function(frag) {
					if (frag.node == fragment.node.parentNode)
						fragment.parent = frag;
					return fragment.parent == null;
				});
				fragment.parent.children.push(fragment);
			}

			// If the node is a boundry node and is excluded, then set a flag to
			// note that such nodes are to be outside of the fragment range.
			fragment.isShared = !(preSplitNode || postSplitNode)
					&& ((node == startNode && startIndex == _nodeLength(node, 1)) || (node == endNode && endIndex == 0));

			if (preSplitNode)
				fragment.preSplitNode = preSplitNode;

			if (postSplitNode)
				fragment.postSplitNode = postSplitNode;

		} // End iterating over affected nodes

		// Determine which nodes are shared with the fragment's range and
		// document and which aren't
		markSharedNodes(fragRoot);

		return fragRoot;

		/**
		 * An inner support function. Sets the "isShared" flag for a fragment
		 * and all it's descendant fragments
		 * 
		 * @param {de.dom.DOMFragment}
		 *            currentFrag The fragment to mark from
		 */
		function markSharedNodes(currentFrag) {

			var isAnyChildShared = false;

			// Scan children first (disconnecting post order)
			for ( var ch in currentFrag.children) {

				var childFrag = currentFrag.children[ch];

				// Recurse
				markSharedNodes(childFrag);

				// Detect if any children of the current node are shared
				isAnyChildShared |= childFrag.isShared;
			}

			// Determine if this fragment is shared. The root is a special
			// case... thus explicitly must
			// be declared as being shared. isShared will already be true if the
			// fragment is a boundry
			// node (start/end) that has been excluded from the range.
			currentFrag.isShared |= (isAnyChildShared
					|| currentFrag == fragRoot || currentFrag.node.childNodes.length != currentFrag.children.length);

		} // End inner markSharedNodes

	}
	; // End buildFragment

	// Expose build fragment
	de.buildFragment = _buildFragment;

	// End Fragment.js
	// Start OperationManager.js
	/* Read Only. Stores all undoable/redoable operation logic  */
	var _operationRepository = {}, 

		/* Read Only. Stores the current list of operations. Null/undefined if there is none */
		_curOperationList,
	    
	    /* Set to true to record operations, false to ignore. Defaults to true. */
	    _recordOperations = true;

	/**
	 * 
	 * @param {Number} opCode    The unique operation code which identifies the operation.
	 * 
	 * @param {Function} exec    The execution function. The first argument will be the operation data.
	 *                           The following arguments are specific execution arguemnts to the operation.
	 * 
	 * @param {Function} undo    The undo function. Given one argument: the operation data.
	 * 
	 * @param {Function} redo    The redo function. Given one argument: the operation data.
	 * 
	 * @return {Number} The operation code of the registered operation
	 */
	function _registerOperation(opCode, exec, undo, redo) {
		
		// Should generate operation code?
		if (!opCode) {
			opCode = _registerOperation.genOp + 1;
			do {
				opCode++;
			} while(_operationRepository[opCode]);
			_registerOperation.genOp = opCode;
		}
		
		debug.assert(!_operationRepository[opCode], "Attempted to override operation with op code: " + opCode);
		_operationRepository[opCode] = {exec:exec,undo:undo,redo:redo};
		
		return opCode;
	}

	_registerOperation.genOp = 100;

	/**
	 * Executes an undoable operation. If _recordOperations is on then the operation will be stored
	 * in the _curOperationList list.
	 * 
	 * When ever dom is to be manipulated, it is always done here. The additional arguments after the given op code
	 * are specific to the operation.
	 * 
	 * @param {Number} opCode The operation code of the operation to execute.
	 */
	function _execOp(opCode) {
			
		// Get the operation code
		var operation = _operationRepository[opCode];
		debug.assert(operation != null, "Unknown operation: " + opCode);
		
		// Create argument list... begin with the operation data object
	    var args = Array.prototype.slice.call(arguments);
	    args.shift();
		args.unshift({opCode:opCode});
	    
		// Execute the operation
		var opRes = operation.exec.apply(operation, args);

		// Store the operation data
	     if (_recordOperations) {
	    	// Create a new operation list if not appending
	    	if (!_curOperationList)
	    		_curOperationList = [];
	         _curOperationList.push(args[0]);
	     }
		
		// Return the operatoin-specific result
		return opRes;
		
	}

	/**
	 * Note: Wipes current operation list.
	 * @return {[Object]} The current list of operations. Null if there are none.
	 */
	function _getOperations() {
		var ops = _curOperationList;
		_curOperationList = null;
		return ops;
	}

	/**
	 * Undoes a list of operations. Assumes that the effected DOM state is the same as it was after the operations were executed.
	 * 
	 * @param {[Object]} opList The list of operations to undo
	 */
	function _undoOperations(opList) {
		for (var i = opList.length - 1; i >= 0; i--) {
			var opData = opList[i];
			var operation = _operationRepository[opData.opCode];
			debug.assert(operation != null, "Unknown operation: " + opData.opCode);
			operation.undo(opData);
		}
	}

	/**
	 * Redoes a list of operations. Assumes that the effected DOM state is the same as it was after the operations were undone.
	 * 
	 * @param {[Object]} opList The list of operations to redo
	 */
	function _redoOperations(opList) {
		for (var i in opList) {
			var opData = opList[i];
			var operation = _operationRepository[opData.opCode];
			debug.assert(operation != null, "Unknown operation: " + opData.opCode);
			operation.redo(opData);
		}
	}

	/**
	 * Controls whether undoable operations should be recorded.
	 * ONLY USE IF YOU KNOW WHAT YOU ARE DOING.
	 * 
	 * TODO: Detailed doc.
	 * 
	 * @param {Boolean} on True to turn on operation recording. False to turn off.
	 */
	de.recordOperations = function(on) {
	    _recordOperations = on;
	};

	/* BASE OPERATIONS */

	// @DEBUG ON
	_Operation = {
		// @REPLACE _Operation.INSERT_NODE 1
		INSERT_NODE : 1,
		
		// @REPLACE _Operation.REMOVE_NODE 2
		REMOVE_NODE : 2,
		
		// @REPLACE _Operation.SPLIT_TEXT_NODE 3
		SPLIT_TEXT_NODE : 3,
		
		// @REPLACE _Operation.INSERT_TEXT 4
	    INSERT_TEXT : 4,
		
		// @REPLACE _Operation.REMOVE_TEXT 5
	    REMOVE_TEXT : 5,
		
		// @REPLACE _Operation.SET_CSS_STYLE 6
	    SET_CSS_STYLE : 6,
	    
	    // @REPLACE _Operation.SET_CLASS 7
	    SET_CLASS : 7,
	    
	    // @REPLACE _Operation.INSERT_ROW 8
	    INSERT_ROW : 8,
	    
	    // @REPLACE _Operation.INSERT_CELL 9
	    INSERT_CELL : 9,
	    
	    // @REPLACE _Operation.DELETE_ROW 10
	    DELETE_ROW : 10,
	    
	    // @REPLACE _Operation.DELETE_CELL 11
	    DELETE_CELL : 11
	    
	};
	// @DEBUG OFF


	_registerOperation(_Operation.INSERT_NODE, 

		/**
		 * Execute
		 * @param {Object} data      The operation data
		 * @param {Node} newNode     The new dom node to insert
		 * @param {Node} parent      The parent of the dom node to insert into
		 * @param {Number} index     The index in the parent to insert the dom node. Omit to append
		 */
		function(data, newNode, parent, index){
			data.newNode = newNode;
			data.parent = parent;
			if (index || index === 0) // Is it an append operation?
				data.pos = index;
			this.redo(data);
		}, 
		
		/* Undo */
		function(data){
			data.parent.removeChild(data.newNode);
		}, 
		
		/* Redo */
		function(data){
			if (data.pos || data.pos === 0)
				_insertAt(data.parent, data.newNode, data.pos);
			else data.parent.appendChild(data.newNode);
		}
		
	);

	_registerOperation(_Operation.REMOVE_NODE, 
		
		/**
		 * Execute
		 * @param {Object} data    The operation data
		 * @param {Node} target    The Node to remove
		 */
		function(data, target){
			data.parent = target.parentNode;
			data.pos = _indexInParent(target);
			data.target = target;
			this.redo(data);
		}, 
		
		/* Undo */
		function(data){
			_insertAt(data.parent, data.target, data.pos);
		}, 
		
		/* Redo */
		function(data){
			data.parent.removeChild(data.target);
		}
	);


	_registerOperation(_Operation.SPLIT_TEXT_NODE, 
		
		/* Execute */
		function(data, target, index){
			data.target = target;
			data.index = index;
			data.rem = target.splitText(index);
			return data.rem;
		}, 
		
		/* Undo */
		function(data){
			// Restore target nodes full text value
			data.target.nodeValue += data.rem.nodeValue;
			
			// Get rid of the split text node
			data.rem.parentNode.removeChild(data.rem);
			data.rem.nodeValue = ""; // free some memory
		}, 
		
		/* Redo */
		function(data){
			
			var fullText = data.target.nodeValue;
			// Re-set the splitted nodes text
			data.rem.nodeValue = fullText.substr(data.index);
			data.target.nodeValue = fullText.substr(0, data.index);
			
			// Re-insert the split node
			_insertAfter(data.rem, data.target);
		}
	);


	_registerOperation(_Operation.INSERT_TEXT, 
		
		/* Execute */
		function(data, target, text, index){
	        
	        data.target = target;
	        data.index = index;
	        data.len = text.length;
	        
	        var pre = target.nodeValue.substr(0, index), 
	            post = target.nodeValue.substr(index);
	        
	        target.nodeValue = pre + text + post;
		}, 
		
		/* Undo */
		function(data){
	        
	        data.text = data.target.nodeValue.substr(data.index, data.len);
	        
	        var pre = data.target.nodeValue.substr(0, data.index),
	            post = data.target.nodeValue.substr(data.index + data.len);
	            
	        data.target.nodeValue = pre + post
	        
	        delete data["len"]; // Free some memory
		}, 
		
		/* Redo */
		function(data){
	        
	        data.len = data.text.length;
	        
	        var pre = data.target.nodeValue.substr(0, data.index), 
	            post = data.target.nodeValue.substr(data.index);
	        
	        data.target.nodeValue = pre + data.text + post;

	        delete data["text"]; // Free some memory        
		}
	);


	_registerOperation(_Operation.REMOVE_TEXT, 
		
		/* Execute */
		function(data, target, index, length){
	        
	        data.target = target;
	        data.index = index;
	        data.text = target.nodeValue.substr(index, length);
	        
	        var pre = target.nodeValue.substr(0, index),
	            post = target.nodeValue.substr(index + length);
	            
	        target.nodeValue = pre + post
		}, 
	    
	    /* Undo - same as insert text's undo */
	    _operationRepository[_Operation.INSERT_TEXT].redo,
		
		/* Redp - same as insert text's undo  */
	   _operationRepository[_Operation.INSERT_TEXT].undo
	   
	);

	_registerOperation(_Operation.SET_CSS_STYLE, 
		
	   /**
	    * Execute
	    * @param {Object} data    The operation data
	    * @param {Node} target    The target element to set CSS
	    * @param {String} css     The CSS Field to set in javascript notation
	    * @param {String} value   The value of the CSS to set
	    */
		function(data, target, css, value){
	        data.target = target;
	        data.css = css;
	        data.newValue = value;
	        data.oldValue = _engine == _Platform.TRIDENT ? data.target.style.getAttribute(data.css) : data.target.style[data.css];
	        this.redo(data);
		}, 
		
		/* Undo */
		function(data){
	        if (_engine == _Platform.TRIDENT) 
	            data.target.style.setAttribute(data.css, data.oldValue);
	        else 
	            data.target.style[data.css] = data.oldValue;
		}, 
		
		/* Redo */
		function(data){
	        if (_engine == _Platform.TRIDENT) 
	            data.target.style.setAttribute(data.css, data.newValue);
	        else 
	            data.target.style[data.css] = data.newValue;
		}
	);


	_registerOperation(_Operation.SET_CLASS, 
		
	   /**
	    * Execute
	    * @param {Object} data    The operation data
	    * @param {Node} target    The target element to set CSS
	    * @param {String} name    The class name to set (replaces full class name)
	    */
		function(data, target, name){
	        data.target = target;
	        data.newName = name;
	        data.oldName = _getClassName(target);
	        this.redo(data);
		}, 
		
		/* Undo */
		function(data){
	        _setClassName(data.target, data.oldName);
		}, 
		
		/* Redo */
		function(data){
	        _setClassName(data.target, data.newName);
		}
	);


	_registerOperation(_Operation.INSERT_CELL, 
		
	   /**
	    * Execute
	    * @param {Object} data     The operation data
	    * @param {Node} row        The target row element to insert the cell into
	    * @param {Number} index    The index in the row to insert into. Clamped if out of bounds
	    * @return {Node}           The new cell that was created
	    */
		function(data, row, index){
	        data.row = row;
	        data.index = index > row.cells.length ? row.cells.length : index; // Safely clamp range
	        if (data.index < 0) data.index = 0;
	        return this.redo(data);
	            
		}, 
		
		/* Undo */
		function(data){
	        data.row.deleteCell(data.index);
		}, 
		
		/* Redo */
		function(data){
	        return data.row.insertCell(data.index);
		}
	);



	_registerOperation(_Operation.INSERT_ROW, 
		
	   /**
	    * Execute
	    * @param {Object} data     The operation data
	    * @param {Node} table      The target table element to insert the row into
	    * @param {Number} index    The index in the row to insert into. Clamped if out of bounds
	    * @return {Node}           The new row that was created
	    */
		function(data, table, index){
	        data.table = table;
	        data.index = index > table.rows.length ? table.rows.length : index; // Safely clamp range
	        if (data.index < 0) data.index = 0;
	        return this.redo(data);
	            
		}, 
		
		/* Undo */
		function(data){
	        data.table.deleteRow(data.index);
		}, 
		
		/* Redo */
		function(data){
	        return data.table.insertRow(data.index);
		}
	);


	_registerOperation(_Operation.DELETE_ROW, 
		
	   /**
	    * Execute
	    * @param {Object} data     The operation data
	    * @param {Node} table      The target table element to insert the row into
	    * @param {Number} index    The index in the row to delete. Clamped if out of bounds
	    */
		function(data, table, index){
	        data.table = table;
	        data.index = index >= table.rows.length ? table.rows.length-1 : index; // Safely clamp range
	        if (data.index < 0) data.index = 0;
	        this.redo(data);
		}, 
		
		/* Undo */
		function(data){
	        var newRow = data.table.insertRow(data.index);
	        
	        // Migrate contents from old removed row into new row
	        while(data.row.firstChild) {
	            var migrant = data.row.firstChild;
	            data.row.removeChild(migrant);
	            newRow.appendChild(migrant);
	        }
	        
	        // Save memory - get rid of old removed row reference
	        delete data["row"];
		}, 
		
		/* Redo */
		function(data){
	        data.row = data.table.rows[data.index]; // Save row - need to keep contents
	        data.table.deleteRow(data.index);
		}
	);


	_registerOperation(_Operation.DELETE_CELL, 
		
	   /**
	    * Execute
	    * @param {Object} data     The operation data
	    * @param {Node} row        The target row element to delete the cell from
	    * @param {Number} index    The cell index in the row to delete. Clamped if out of bounds
	    */
		function(data, row, index){
	        data.row = row;
	        data.index = index >= row.cells.length ? row.cells.length-1 : index; // Safely clamp range
	        if (data.index < 0) data.index = 0;
	        this.redo(data);
		}, 
		
		/* Undo */
		function(data){
	        var newCell = data.row.insertCell(data.index);
	        
	        // Migrate contents from old removed row into new row
	        while(data.cell.firstChild) {
	            var migrant = data.cell.firstChild;
	            data.cell.removeChild(migrant);
	            newCell.appendChild(migrant);
	        }
	        
	        // Save memory - get rid of old removed row reference
	        delete data["cell"];
		}, 
		
		/* Redo */
		function(data){
	        data.cell = data.row.cells[data.index]; // Save cell - need to keep contents
	        data.row.deleteCell(data.index);
		}
	);
	
	//End OperationManager.js
	//Start Keyboard.js

	(function() {

		// All the keymaps
	    var keymaps = {
	    
	        // maps the charcodes of special printable keys to key identifiers 
	        specialToCharCode: {
	            8: "Backspace", // The Backspace (Back) key.
	            9: "Tab", // The Horizontal Tabulation (Tab) key.
	            //   Note: This key identifier is also used for the
	            //   Return (Macintosh numpad) key.
	            13: "Enter", // The Enter key.
	            27: "Escape", // The Escape (Esc) key.
	            32: "Space" // The Space (Spacebar) key.
	        },
	        
	        // maps the keycodes of non printable keys to key identifiers
	        keyCodeToId: {
	            16: "Shift", // The Shift key.
	            17: "Control", // The Control (Ctrl) key.
	            18: "Alt", // The Alt (Menu) key.
	            20: "CapsLock", // The CapsLock key
	            224: "Meta", // The Meta key. (Apple Meta and Windows key)
	            37: "Left", // The Left Arrow key.
	            38: "Up", // The Up Arrow key.
	            39: "Right", // The Right Arrow key.
	            40: "Down", // The Down Arrow key.
	            33: "PageUp", // The Page Up key.
	            34: "PageDown", // The Page Down (Next) key.
	            35: "End", // The End key.
	            36: "Home", // The Home key.
	            45: "Insert", // The Insert (Ins) key. (Does not fire in Opera/Win)
	            46: "Delete", // The Delete (Del) Key.
	            112: "F1", // The F1 key.
	            113: "F2", // The F2 key.
	            114: "F3", // The F3 key.
	            115: "F4", // The F4 key.
	            116: "F5", // The F5 key.
	            117: "F6", // The F6 key.
	            118: "F7", // The F7 key.
	            119: "F8", // The F8 key.
	            120: "F9", // The F9 key.
	            121: "F10", // The F10 key.
	            122: "F11", // The F11 key.
	            123: "F12", // The F12 key.
	            144: "NumLock", // The Num Lock key.
	            44: "PrintScreen", // The Print Screen (PrintScrn, SnapShot) key.
	            145: "Scroll", // The scroll lock key
	            19: "Pause", // The pause/break key
	            91: "Win", // The Windows Logo key
	            93: "Apps" // The Application key (Windows Context Menu)
	        },
	        
	        // maps the keycodes of the numpad keys to the right charcodes
	        numpadToCharCode: {
	            96: "0".charCodeAt(0),
	            97: "1".charCodeAt(0),
	            98: "2".charCodeAt(0),
	            99: "3".charCodeAt(0),
	            100: "4".charCodeAt(0),
	            101: "5".charCodeAt(0),
	            102: "6".charCodeAt(0),
	            103: "7".charCodeAt(0),
	            104: "8".charCodeAt(0),
	            105: "9".charCodeAt(0),
	            106: "*".charCodeAt(0),
	            107: "+".charCodeAt(0),
	            109: "-".charCodeAt(0),
	            110: ".".charCodeAt(0),
	            111: "/".charCodeAt(0)
	        },
	        
	        // Helpers
	        charCodeA: "A".charCodeAt(0),
	        charCodeZ: "Z".charCodeAt(0),
	        charCodea: "a".charCodeAt(0),
	        charCodez: "z".charCodeAt(0),
	        charCode0: "0".charCodeAt(0),
	        charCode9: "9".charCodeAt(0),
	    
			// Platform dependant maps
			keyCodeFix : {},
			charCodeToKeyCode : {},
	        
	        // Maps keycodes that cannot be distinguished in key press events, to the other keycodes that have also
	        // been assigned to the same key in the key press event. ALl key codes (keys/values) in the map will be
	        // simulated as key presses in the key down event.
	        ambiguousKeyPressCodes : {} 
	    
	    };

		// Construct inverse maps
		keymaps.idToKeyCode = {};
		for (var key in keymaps.keyCodeToId) {
			keymaps.idToKeyCode[keymaps.keyCodeToId[key]] = parseInt(key, 10);
		}
		for (var key in keymaps.specialToCharCode) {
			keymaps.idToKeyCode[keymaps.specialToCharCode[key]] = parseInt(key, 10);
		}

	    // Setup platform dependant key maps
	    switch (_engine) {
	        
	        case _Platform.TRIDENT: // MSHTML
	            keymaps.charCodeToKeyCode = {
	                13: 13,
	                27: 27
	            };
	            break;
	        
	        case _Platform.GECKO:
	            keymaps.keyCodeFix = {
	                12: idToKeyCode("NumLock")
	            };
	            break;
	            
	        case _Platform.WEBKIT:

	            // starting with Safari 3.1 (version 525.13) Apple switched the key
	            // handling to match the IE behaviour.
	            if (_Platform.engineVersion && _Platform.engineVersion < 525.13) { // TODO: Check if safari?
	                keymaps.charCodeToKeyCode = {
	                
	                    // Safari/Webkit Mappings
	                    63289: idToKeyCode("NumLock"),
	                    63276: idToKeyCode("PageUp"),
	                    63277: idToKeyCode("PageDown"),
	                    63275: idToKeyCode("End"),
	                    63273: idToKeyCode("Home"),
	                    63234: idToKeyCode("Left"),
	                    63232: idToKeyCode("Up"),
	                    63235: idToKeyCode("Right"),
	                    63233: idToKeyCode("Down"),
	                    63272: idToKeyCode("Delete"),
	                    63302: idToKeyCode("Insert"),
	                    63236: idToKeyCode("F1"),
	                    63237: idToKeyCode("F2"),
	                    63238: idToKeyCode("F3"),
	                    63239: idToKeyCode("F4"),
	                    63240: idToKeyCode("F5"),
	                    63241: idToKeyCode("F6"),
	                    63242: idToKeyCode("F7"),
	                    63243: idToKeyCode("F8"),
	                    63244: idToKeyCode("F9"),
	                    63245: idToKeyCode("F10"),
	                    63246: idToKeyCode("F11"),
	                    63247: idToKeyCode("F12"),
	                    63248: idToKeyCode("PrintScreen"),
	                    3: idToKeyCode("Enter"),
	                    12: idToKeyCode("NumLock"),
	                    13: idToKeyCode("Enter")
	                };
	                
	            } else { // Modern versions of webkit
	                keymaps.charCodeToKeyCode = {
	                    13: 13,
	                    27: 27
	                };
	            }

	            break; 
	            
	            case _Platform.PRESTO:
	            
	                keymaps.ambiguousKeyPressCodes = {
	                    35: 51, // # <=> End
	                    36: 52, // $ <=> Home
	                    44: 188, // Comma <=> Print Screen
	                    45: 109, // - <=> Insert
	                    46: 190, // Period <=> Delete
	                    91: 219, // [ <=> Windows
	                    93: 221  // ] <=> Apps
	                };

	                // Add presto specific maps...
	                
	               // Inverse ambigious key codes to simulate a keypress for in a key down event,
	               // True values indicate they only should be simulated if the shift modifier is down.
	               // False values are implicit (the remainding ambiguios codes) and should only be simulated
	               // if the shift modifier is not down.
	                keymaps.prestoSimulateInvOnShift = {
	                    51: 1, // #
	                    52: 1 // $
	                    // 188 : 0 // ,
	                    // 109 : 0 // -
	                    // 190 : 0 // period
	                    // 219 : 0 // [
	                    // 221 : 0 // ]
	                };
	                
	                // The "which" codes to use (as well as alpha numeric codes) as char codes on key presses.
	                keymaps.prestoUseWhichCodes = {
	                   33: 1, // ! <=> Page Up
	                   34: 1, // " <=> Page Down
	                   40: 1, // ( <=> Down
	                   39: 1, // ' <=> Right
	                   38: 1, // & <=> Up
	                   37: 1, // % <=> Left
	                   123: 1 // { <=> F12
	                };
	    
	            break;
	    }
	    
	    // Create inverse maps
	    keymaps.ambiguousKeyPressCodesInv = {};
	    for (var key in keymaps.ambiguousKeyPressCodes) {
	        keymaps.ambiguousKeyPressCodesInv[keymaps.ambiguousKeyPressCodes[key]] = parseInt(key, 10);
	    }
	        

		/**
		 * @class A singleton that provides cross-browser/platform keyboard-normalization facilities
	 	 * @author Brook Novak
		 */
		de.events.Keyboard = {
	            
	            
	    	/**
	    	 * Returns the event's "normalizedKey" to the DOM Level 3 Spec of keyIdentifier.
	    	 * @param {Event} domEvent The dom event to "normalize"
	    	 * 
	    	 * @param {Boolean} isKeyDown True if the dom event for keydown, false if for keypress.
	    	 * 
	    	 * @return {String} The key identifier for the given key.
	    	 *                  Null if the key ident is unknown or should be extracted from a different event type
	    	 */
	    	 getKeyIdentifier : function(domEvent, isKeyDown) {
	     
	    		var keyCode, // non printable keys. e.g. CTRL, INSERT
	                charCode, // printable symbols. e.g. A,Z,&
	                useGenericMapping = false;
	    		
	            //debug.println((isKeyDown ? "Keydown" : "KeyPress") + ": cc=" + domEvent.charCode + "kc=" + domEvent.keyCode);
	            
	     		switch(_engine) {
	                
	    			case _Platform.TRIDENT:
	        
	                    if (isKeyDown && (isNonPrintableKeyCode(domEvent.keyCode) || domEvent.keyCode == 8 || domEvent.keyCode == 9)) 
	                         keyCode = domEvent.keyCode;                    
	                
	                    // Use keydown if CTRL is down, but keyPress if CTRL is not down
	                    if ((!isKeyDown && !domEvent.ctrlKey) || (isKeyDown && domEvent.ctrlKey)) {
	        		        if (keymaps.charCodeToKeyCode[domEvent.keyCode]) 
	        					keyCode = keymaps.charCodeToKeyCode[domEvent.keyCode];
	        		        else 
	                            charCode = domEvent.keyCode;
	                    }
	    
	    				break;
	    			
	    			case _Platform.GECKO:
	                    if (isKeyDown) {
	                        // Moz doesn't get keypress events for CTRL, ALT or SHIFT,
	                        // So raise them on key down
	                        if (domEvent.keyCode >= 16 && domEvent.keyCode <= 18) 
	                           keyCode = domEvent.keyCode;
	                           
	                    } else {
	                        keyCode = keymaps.keyCodeFix[domEvent.keyCode] || domEvent.keyCode;
	                        charCode = domEvent.charCode;
	                    }
	    				break;
	    			
	    			case _Platform.WEBKIT:
	                
	                    if (_browser == _Platform.SAFARI) {
	                        
	                        if (isKeyDown) {
	                           
	                           
	                            if (_engineVersion && _engineVersion < 525.13)
	                                keyCode = keymaps.charCodeToKeyCode[domEvent.charCode] || domEvent.keyCode;
	                           
	                            else keyCode = domEvent.keyCode;
	                            
	                            if (!isNonPrintableKeyCode(keyCode) && !this.isAcceleratorDown(domEvent))
	                                keyCode = 0;
	                            
	                        } else { // Key Press get printable charactors
	                        
	                            // starting with Safari 3.1 (verion 525.13) Apple switched the key
	                            // handling to match the IE behaviour.
	                            if (_engineVersion && _engineVersion < 525.13) {
	                            
	                                if (keymaps.charCodeToKeyCode[domEvent.charCode]) 
	                                    keyCode = keymaps.charCodeToKeyCode[domEvent.charCode];
	                                else 
	                                    charCode = domEvent.charCode;
	                                
	                            } else {
	                            
	                                if (keymaps.charCodeToKeyCode[domEvent.keyCode]) 
	                                    keyCode = keymaps.charCodeToKeyCode[domEvent.keyCode];
	                                else 
	                                    charCode = domEvent.keyCode;
	                            }                        
	                            
	                        }
	                        
	                    } else if(_browser == _Platform.CHROME) {
	                      
	                       // Chrome is good, it sets keycode,charcode,which and keyidentifier..
	                       if (isKeyDown) {
	                           
	                           // Keycodes can be detected from keydowns
	                           if (isNonPrintableKeyCode(domEvent.keyCode))
	                               keyCode = domEvent.keyCode;
	                           
	                           // If the accelerator key is down while pressing printable keys the charcodes
	                           // appear as key codes in the keydown event
	                           else if (this.isAcceleratorDown(domEvent))
	                               charCode = domEvent.keyCode;
	                               
	                       } else {
	                           
	                           // Printable keys (charcodes) occur in the keypress event (except when
	                           // the accelerator is down).
	                           if (domEvent.charCode && !this.isAcceleratorDown(domEvent)) 
	                               charCode = domEvent.charCode;
	                       
	                       }
	                        
	                    } else useGenericMapping = true;
	      
	    				break;
	    				
	    			case _Platform.PRESTO:
	                                    
	                    if (isKeyDown) {
	    
	                        if (keymaps.ambiguousKeyPressCodesInv[domEvent.keyCode]) {
	                            // Avoid simulating key codes which will have a following legit keypress event 
	                            var simOnShift = keymaps.prestoSimulateInvOnShift[domEvent.keyCode];
	                            if ((domEvent.shiftKey && simOnShift) || (!domEvent.shiftKey && !simOnShift))
	                                charCode = keymaps.ambiguousKeyPressCodesInv[domEvent.keyCode];
	                            
	                        } else if (keymaps.ambiguousKeyPressCodes[domEvent.keyCode])     
	                            keyCode = domEvent.keyCode;
	                        
	                    } else { // Key press
	                    
	                        if (domEvent.which && (isAlphaNumericAscii(domEvent.which) || keymaps.prestoUseWhichCodes[domEvent.which])) 
	                            charCode = domEvent.which;
	                        else if (keymaps.keyCodeToId[domEvent.keyCode]) 
	                			keyCode = domEvent.keyCode;
	        				else 
	        					charCode = domEvent.keyCode;                    
	                    }
	    				break;
	                    
	               case _Platform.KHTML:
	                   
	                       // TODO
	                    useGenericMapping = true;
	                        
	                    break;                
	    				
	    			default:
	                    useGenericMapping = true;
	    		} // End switch
	    		
	            if (useGenericMapping && isKeyDown) {
	                
	                if (domEvent.keyIdentifier && domEvent.keyIdentifier.length > 0) { 
	                    // See http://www.w3.org/TR/DOM-Level-3-Events/keyset.html
	                    
	                    // Is the key identifier unicode-encoded?
	                    var ucMatch = /^U\+([\dA-Fa-f]+)$/.exec(domEvent.keyIdentifier);
	                    
	                    if (ucMatch) {
	                        
	                        // Extract unicode numerical value
	                        var uniNum = parseInt(ucMatch[1], 16);
	                        
	                        // Convert into printable symbol
	                        var printable = String.fromCharCode(uniNum);
	                        
	                        if (domEvent.shiftKey) 
	                            return printable.toUpperCase();
	                        else return printable.toLowerCase();
	                        
	                    } else return domEvent.keyIdentifier;
	                    
	                } else {
	                    keyCode = domEvent.keyCode || domEvent.which;
	                    charCode = domEvent.charCode;
	                }
	                
	            }
	    		
	    	     if (keyCode) { // Use keyCode
	    	     
	                 // Omit ambiguous key codes: where actual key presses cannot be distinguished since this
	                 // platform assigns some single key codes to multiple keys.
	                 if (!isKeyDown && keymaps.ambiguousKeyPressCodes[keyCode]) 
	                     return null;
	    		  	
	    	         return keyCodeToId(keyCode);
	    
	    	      } else if (charCode)  // Use charCode
	    	        return charCodeToId(charCode);
	    
	    		return null;
	    	},
	    
	         /**
	          * @param {Event} domEvent A dom event
	          * @return {Boolean} True if the accelerator key was down for the given event.s
	          */
	         isAcceleratorDown : function(domEvent) {
	              // Add platform specific accelerator flag
	              return _os == _Platform.MAC ? domEvent.metaKey : domEvent.ctrlKey;
	         }
	 
	    }; // End Keyboard singleton

	    /**
	     * @param {Number} keyCode A key code to test
	     * @return {Boolean} True if the key code is non-printable. I.E. not a printable symbol.
	     */
	    function isNonPrintableKeyCode(keyCode) {
	      return typeof keymaps.keyCodeToId[keyCode] == "string" || typeof keymaps.specialToCharCode[keyCode] == "string";
	    }

	    /**
	     * @param {Number} keyCode a keycode to test
	     * @return {Boolean} True if the given keycode is identifiable.
	     */	
		function isIdentifiableKeyCode(keyCode) {
	        return isAlphaNumericAscii(keyCode) ||
	               keymaps.specialToCharCode[keyCode] || /* Enter, Space, Tab, Backspace */
	               keymaps.numpadToCharCode[keyCode] ||  /* Numpad */
	               isNonPrintableKeyCode(keyCode);       /* non printable keys */
	    }
	    
	    /**
	     * @param {Number} code A char-code (or which-code) to test
	     * @return {Boolean} True if the given code is alphanumeric
	     */
	    function isAlphaNumericAscii(code) {
	        return (code >= keymaps.charCodeA && code <= keymaps.charCodeZ) || /* Upper */
	               (code >= keymaps.charCodea && code <= keymaps.charCodez) || /* Lower */
	               (code >= keymaps.charCode0 && code <= keymaps.charCode9);   /* Numbers */
	    }
		
	    /**
	     * @param {Number} charCode A charactor code
	     * @return {String} A key identifier for the given charactor code. Undefined if none exists.
	     */
		function charCodeToId(charCode) {
		  return keymaps.specialToCharCode[charCode] || String.fromCharCode(charCode);	
		}

	    /**
	     * @param {Number} keyCode A keycode
	     * @return {String} A key identifier for the given key code. Undefined if none exists.
	     */	
	    function keyCodeToId(keyCode) {
	   	
	      if (isIdentifiableKeyCode(keyCode)) {
		  	
	        var numPadCharCode = keymaps.numpadToCharCode[keyCode];

	        if (numPadCharCode) 
	          return String.fromCharCode(numPadCharCode);

	        return (keymaps.keyCodeToId[keyCode] || keymaps.specialToCharCode[keyCode] || String.fromCharCode(keyCode));
	      
		  } else return null;
	      
	    }
		
	    /**
	     * @param {String} keyId The key identifier string to get the keycode for
	     * @return {Number} The standadized keycode of the given key identifier
	     */
		function idToKeyCode(keyId) {
	      return keymaps.idToKeyCode[keyId] || keyId.charCodeAt(0);
	    }
		


	}) ();

	// End Keyboard.js
	//Start Selection.js
	var _toggleSectionHighlight;

(function(){

    /*
     *  The selection model
     *  
     *  Users can select anything in the document, even GUI's etc. The selection is not native, but is a emulated
     *  model which works on all browsers. The reason while it is emulated is because when the cursor module manipulates
     *  the dom around the clicked nodes - the native selection models fall over and the selection goes haywire on every platform.
     *  
     *  Usually in a typical content editor the cursor follows the end-of-selection. However this is confusing for users
     *  when a blinking cursor is outside editable sections since it suggests that users can edit non-editable html. 
     *  To avoid this confusion the cursor is hidden when the selection contains non-editable content.
     */
    
    /*    
     * The selection start/end node/indexes are virtual. Virtual node/indexes are nodes/index in the document
     * when there is no highlighed dom. Actual node/indexes are nodes/indexes in the document at the current state
     * which is effected by highlighted dom.
     */
    var selStartNode = null, 
        selStartIndex = null, 
        selEndNode = null, 
        selEndIndex = null, 
        highlightFragment = null, 
		fragmentOpList = null,
        formatOpList = null, 
        hightlightCSS = {
            /*high: "#1C1C1C",
            low: "#FFFFFF"*/
			high: "#3B4B5B",
            low: "#DFFFFF"
        }, 
        settingCursor = false,
        
        /* Used for raising selection start/end MVC events. */
        supressSelectionEvents = false,
	    
	    /* 
	     * Determines how many pixels away from a cursor's charactor/element
	     * the mouse pointer should be to re-evaluate a new position.
	     */
	    CURSOR_REEVALAUTE_TOLERANCE = 3,
		
		/* Elements to let clicks fall through */
		fallthroughElements = $createLookupMap("button,input,select,textarea"),
	    
	    /* Inline elements which should not be included/bundled with a word (for word selection) */
	    wordBreakerInlines = $createLookupMap("br,button,img,iframe,map,object,select,textarea,applet"), /* TODO: REFACTOR- SHARE WITH WHITESPACE INTERNALS */
		
        wordBreakerChars = /^\W$/, // TODO: Multilingual support - not just latin alphabet
        
		/* Elements used for focus/selection stealing */
		focusContainer, focusStealerEle,
        
        /* True if the last mouse down was in a protected node. False if not. */
        clickedProtectedNode;
    
    $enqueueInit("Selection", function(){
        
         // Make as subject
         _model(de.selection);
    
        // Disable selection in IE
        if (typeof docBody.onselectstart != "undefined")
            docBody.onselectstart = function(){
                return de.events.consume(window.event)
            };
        
        var target = _engine == _Platform.GECKO ? window : document;
        
        _addHandler(target, "mousedown", onMouseDown);
        _addHandler(target, "mouseup", onMouseUp);
        _addHandler(target, "mousemove", onMouseMove);
        _addHandler(target, "dblclick", onDoubleClick);
        
        // Consume ACCEL+A events to prevent select-all
        _addHandler(document, "keydown", function(e) {
            
            if (de.events.Keyboard.isAcceleratorDown(e)) {
                if (e.keyCode == 65) 
                    return false; // NB: Doesn't work in presto
            }
        });
        
        // Whenever the cursor is set outside of this module, keep the selection synchronized.
        de.cursor.addObserver({
            onCursorChanged: function(cDesc){
                if (settingCursor)                     
                    return;
                if (cDesc) {
                    // Get the cursors virtual node/index
                    var vni = getVirtualNodeIndex(cDesc.domNode, getSelectionIndexFromCDesc(cDesc));
                    
                    // Update the selection: If shift is down then set the new range, otherwise
                    // set selection as a single point.
                    if (de.events.current && de.events.current.shiftKey && selStartNode) 
                        de.selection.setSelection(selStartNode, selStartIndex, vni.node, vni.index, false);
                    else 
                        de.selection.setSelection(vni.node, vni.index, null, null, false);
                        
                } else 
                    de.selection.clear();
            }
        });
		
		// Always ensure that the selection is cleared before an action is executed/redone/undone
		function onBeforeAction() {
			de.selection.clear();
		}
        
        de.UndoMan.addObserver({
            onBeforeExec : onBeforeAction,
            onBeforeUndo : onBeforeAction,
            onBeforeRedo : onBeforeAction
        });
        
		// Setup the focus steal element
		focusContainer = $createElement("div");
        _setClassName(focusContainer, _PROTECTED_CLASS);
		focusContainer.innerHTML = '<input type="text" style="border-style:none"\>';
		_setFullStyle(focusContainer, "position:absolute;width:1px;height:1px;display:none;z-index:-500");
		focusStealerEle = focusContainer.firstChild;
		docBody.appendChild(focusContainer);
		
    }, "Cursor", "UndoMan");
    
    /**
     * @param {Event} e A mouse down dom event
     */
    function onMouseDown(e){
        
        clickedProtectedNode = 0;
		
		var targetNode = de.events.getEventTarget(e);
		
		// Test if should let event fall through
		var nodeName = _nodeName(targetNode);
		if (targetNode && fallthroughElements[nodeName]) {
			
			// Alow selection in text boxes
			if (nodeName == "textarea" || (nodeName == "input" && targetNode.type == "text")) {
				
				// Get ride of dedit selection/cursor - switch edit paradigm to native text box
				de.selection.clear(); // clear selection
				de.cursor.setCursor(null); // clear focus
				
			} 
			return;
		}
		
        if (de.events.Mouse.isLeftDown()) {
            
            // Ignore clicks in protected nodes
            if (de.doc.isProtectedNode(targetNode)) {
                clickedProtectedNode = 1;
                return;
            }
            
            // Get the cursor position at the mouse x/y coord
            var mousePos = de.events.getXYInWindowFromEvent(e),
                targetCursorDesc = de.cursor.getCursorDescAtXY(mousePos.x, mousePos.y, targetNode);

            if (!targetCursorDesc) {
                // Clear any cursor/selection
                de.cursor.setCursor(null); // Triggers MVC event and selection will update
                return false;
            }
            
            // Is the user ranging a selection via the shift key?
            if (e.shiftKey && selStartNode) {
                
                // Try and the cursor at the click position
                settingCursor = true; // Prevent updating selection due to cursor MVC events
                de.cursor.setCursor(targetCursorDesc);
                settingCursor = false;
                
                // Update the selection
                var vni = getVirtualNodeIndex(targetCursorDesc.domNode, getSelectionIndexFromCDesc(targetCursorDesc));
                setSelection(selStartNode, selStartIndex, vni.node, vni.index, false);
                
            } else { // User is clicking in the document
                
                // Set the cursor at the click position
                de.cursor.setCursor(targetCursorDesc); // Triggers MVC event and selection will update
                
                // If the cursor was not supported at the target node, then the selection will have cleared... however
                // we want to allow for the user to select outside of editable sections
                if (!de.cursor.exists() && !de.doc.isProtectedNode(targetCursorDesc.domNode)) {
                    var vni = getVirtualNodeIndex(targetCursorDesc.domNode, getSelectionIndexFromCDesc(targetCursorDesc));
                    setSelection(vni.node, vni.index, null, null, false);
                }
                
            }
            
            // Ensure that the document has focus... this will ensure that any input controls within the
			// document or in the browser loses focus so user input is forwarded to direct edit

	        // Get the scrollbar state and set the focus stealer position in the viewport
	        // to avoid scrolling the document
	        var scrollPos = _getDocumentScrollPos();
	
	        // Position the float (container) at the top left of the viewport,
	        // but if the scroll bars are at zero, then place the float 
	        // outside of the document... this will completely conceal the float
	        focusContainer.style.left = (scrollPos.left == 0 ? -50 : scrollPos.left + 10) + "px";
	        focusContainer.style.top = (scrollPos.top == 0 ? -50 : scrollPos.top + 10) + "px";
			focusContainer.style.display = "";
			
			focusStealerEle.focus();
			focusStealerEle.select();
			
			focusContainer.style.display = "none";

            // Disable native selection
            return false;
        }
             
    }

    /**
     * Implements manipulating of selection via dragging the mouse.
     * @param {Event} e A mouse move dom event
     */
    function onMouseMove(e){
        
        if (de.events.Mouse.isLeftDown() && selStartNode && !clickedProtectedNode) { // Is the user dragging the mouse - and changing the selection?
            
            // Avoid firing many selection event whenever the selection changes while dragging
            supressSelectionEvents = true;

            var mousePos = de.events.getXYInWindowFromEvent(e),
                curCDesc = de.cursor.getCurrentCursorDesc();
            
            // Quick-check to see if the mouse pointer is not far from the current cursor
            // to avoid re-evaluting the cursors poistion via the relatively expensive dual binsearch
            // at every mouse move event:
            if (curCDesc &&
                mousePos.x >= (curCDesc.x - CURSOR_REEVALAUTE_TOLERANCE) &&
                mousePos.x <= (curCDesc.x + curCDesc.width + CURSOR_REEVALAUTE_TOLERANCE) &&
                mousePos.y >= (curCDesc.y - CURSOR_REEVALAUTE_TOLERANCE) &&
                mousePos.y <= (curCDesc.y + curCDesc.height + CURSOR_REEVALAUTE_TOLERANCE)) {
                    
                var updateSelection = 0;
            
                // See is isRightOf flag needs flipping
                if (Math.abs(mousePos.x - curCDesc.x) < Math.abs(mousePos.x - (curCDesc.x + curCDesc.width))) {
                
                    // The mouse is closer to the left of charactor/element that the cursor is currently at
                    if (curCDesc.isRightOf) {
                        // Need to flip the rightOf flag
                        curCDesc.isRightOf = false; // flip
                        settingCursor = true;
                        de.cursor.setCursor(curCDesc);
                        settingCursor = false;
                        updateSelection = 1;
                        
                    }
                    
                } else if (!curCDesc.isRightOf) {
                    // The mouse is closer to the right of charactor/element that the cursor is currently at..
                    // but the current cursor is to the left of it.
                    curCDesc.isRightOf = true; // flip
                    settingCursor = true;
                    de.cursor.setCursor(curCDesc);
                    settingCursor = false;
                    updateSelection = 1;
                }
                
                // Update the selection
                if (updateSelection) {
                    var vni = getVirtualNodeIndex(curCDesc.domNode, getSelectionIndexFromCDesc(curCDesc));
                    setSelection(selStartNode, selStartIndex, vni.node, vni.index, false);
                }
                
            } else { // Re-evaluate the cursor's position via coordinates from the mouse event

                curCDesc = de.cursor.getCursorDescAtXY(mousePos.x, mousePos.y, de.events.getEventTarget(e));

                if (curCDesc && !de.doc.isProtectedNode(curCDesc.domNode)) {
                    
                    var vni = getVirtualNodeIndex(curCDesc.domNode, getSelectionIndexFromCDesc(curCDesc));
                    
                    // Update the selection
                    setSelection(selStartNode, selStartIndex, vni.node, vni.index);

                }
                
            }
        }
    }
    
    
    /**
     * Raises selection changed events for dragged selection
     * @param {Object} e
     */
    function onMouseUp(e) {
        
        // Was there any selection due to dragging the moust pointer?
        if (selStartNode && supressSelectionEvents) 
            de.selection.fireEvent("SelectionChanged");
        
        // Restore selection supression flag
        supressSelectionEvents = false;
    }
    
	
    // TODO: Triple-click to select full phraise
    function onDoubleClick(e) {
        
        // Ignore clicks in protected nodes
        if (!de.doc.isProtectedNode(de.events.getEventTarget(e))) {
            
            de.selection.clear();
            
            var mousePos = de.events.getXYInWindowFromEvent(e);
            var cDesc = de.cursor.getCursorDescAtXY(mousePos.x, mousePos.y, de.events.getEventTarget(e));
            
            // Double-clicked on anything to select?
            if (cDesc) {
            
                // Get the word the user selected on if any
                var range = de.selection.getWordRangeAt(cDesc.domNode, cDesc.relIndex);
                if (range) { // double clicked on word / space
                    // Set the new selection to select the word
                    setSelection(range.startNode, range.startIndex, range.endNode, range.endIndex);
                    
                }
                
            }
        }

        return false; // TODO: DOES Disable selection in safari?
    }
    
    // See namespace docs
    function getVirtualNodeIndex(node, index) {
         if (highlightFragment)
             return highlightFragment.getOriginalNodeIndex(node, index);
        return {node:node,index:index};
    }
    
    // See namespace docs
    function getActualNodeIndex(node, index) {
        
         if (highlightFragment)
             return highlightFragment.getAdjustedNodeIndex(node, index);
        return {node:node,index:index};
        
    }
    
    /**
     * To be used when wanting the highlighted DOM cleared.
     * ONLY TO BE USED IN BRIEF MOMENTS: Always call the false then truee, never leave
     * in uneven state.
     * 
     * @param {Boolean} on True to restore highlighting, false to clear any.
     */
    _toggleSectionHighlight = function(on) {
        
        if (on && highlightFragment) {
             // Restore highlight css
             _redoOperations(formatOpList);
        } else if (!on && highlightFragment) {
            // Remove highlight formatting
            _undoOperations(formatOpList);
        }
        
    }
    

    /**
     * Visually highlights the current selection. Assumes that there is no selection highlight formatting.
     * Updates the cursor.
     */
    function highlightSelection(){
        
        debug.assert(formatOpList == null && fragmentOpList == null && highlightFragment == null);
        debug.assert(selStartNode != null && selEndNode != null);
 
        // Gets range in left-to-right order       
        var selRange = de.selection.getRange(true);

        debug.assert(!_getOperations());
        
        try {
        
            // Get the cursor cursor state
            var curCDesc = de.cursor.getCurrentCursorDesc();
            
			highlightFragment = _buildFragment(_getCommonAncestor(selRange.startNode, selRange.endNode), 
                selRange.startNode, 
                selRange.startIndex, 
                selRange.endNode, 
                selRange.endIndex);
				
			// Get fragment build operations
			fragmentOpList = _getOperations() || [];
			
            // Apply the CSS Highlighting
            highlightFragment.visit(function(frag){
            
                if (!frag.isShared) {
                
                    var domNode = frag.node, highlightNode = null;
                    
                    if (domNode.nodeType == Node.ELEMENT_NODE)
						highlightNode = domNode;
                        
                    else if (domNode.nodeType == Node.TEXT_NODE && frag.parent.isShared && _doesTextSupportNonWS(domNode)) { 
                    
                        // If this non shared fragment is a text node who's parent is shared, then
                        // in order to format this node then spans will be added as its parent
                        highlightNode = $createElement("span");
                        highlightNode.className = "dehighlight-node";
                        
                        // Add the format span and move the text node into it
                         _execOp(_Operation.INSERT_NODE, highlightNode, domNode.parentNode, _indexInParent(domNode));
                         _execOp(_Operation.REMOVE_NODE, domNode);
                         _execOp(_Operation.INSERT_NODE, domNode, highlightNode);
                    }
					
					if (highlightNode) {
                        
                        // TODO : Use Classes instead?? although setting actual style will have best precedence
						
						// Get background color for this element
						/*var bgColor, bgNode = highlightNode;
						do {
							bgColor = _getComputedStyle(bgNode, "background-color");
							bgNode = bgNode.parentNode;
						} while (bgNode && bgColor == "transparent"); // CSS Defaut for background color
						
						if (bgColor && bgColor != "") {
							bgColor = _getColorRGB(bgColor);
						} else 
							bgColor = [255, 255, 255];
						
						// Get bg color brightness
						var intensity = ((bgColor[0] / 255) + (bgColor[1] / 255) + (bgColor[2] / 255)) / 3;
						
						
						// Override/set background and foreground color style for this element
						_execOp(_Operation.SET_CSS_STYLE, highlightNode, "backgroundColor", intensity >= 0.5 ? hightlightCSS.high : hightlightCSS.low);
						_execOp(_Operation.SET_CSS_STYLE, highlightNode, "color", intensity >= 0.5 ? hightlightCSS.low : hightlightCSS.high);
						*/
						// ABOVE KILLS PERFORMANCE
						
                       // if (!_isBlockLevel(highlightNode) || !_isAncestor(highlightNode, selRange.endNode)) {
                            _execOp(_Operation.SET_CSS_STYLE, highlightNode, "backgroundColor", hightlightCSS.high);
                            _execOp(_Operation.SET_CSS_STYLE, highlightNode, "color", hightlightCSS.low);
                       // }
						
					}
				}

            }); // End visiting all fragments

            // Get the formatting operations
            formatOpList = _getOperations() || [];
            
            // Update the new cursor pos -If there is a cursor, then its node/index may need updating
            if (curCDesc) {
                var cursorANI = getActualNodeIndex(curCDesc.domNode, curCDesc.relIndex);
                if (cursorANI.node != curCDesc.domNode || cursorANI.index != curCDesc.relIndex) {
                    curCDesc.domNode = cursorANI.node;
                    curCDesc.relIndex = cursorANI.index;
                    settingCursor = true;
                    de.cursor.setCursor(curCDesc);
                    settingCursor = false;
                }
            }
            
        } catch (e) {
            settingHighlight = false;
            selStartNode = selEndNode = highlightFragment = null;
            formatOpList = null;
            throw e;
        }

    }
    
    /**
     * Un-highlights any previous highlighting if there is any.
     * Updates the cursor.
     */
    function unHighlightSelection(){
        
        if (highlightFragment) {
            
            // Keep the cursor updated
            var curCDesc = de.cursor.getCurrentCursorDesc();
            var cursorVNI = curCDesc ? getVirtualNodeIndex(curCDesc.domNode, curCDesc.relIndex) : null;
              
            _undoOperations(formatOpList);
			_undoOperations(fragmentOpList);
            formatOpList = fragmentOpList = highlightFragment = null;

            // If there was a cursor, then update its node / index due to highlighting
            if (cursorVNI && (cursorVNI.node != curCDesc.domNode || cursorVNI.index != curCDesc.relIndex)) {
                curCDesc.domNode = cursorVNI.node;
                curCDesc.relIndex = cursorVNI.index;
                settingCursor = true;
                de.cursor.setCursor(curCDesc);
                settingCursor = false;
            }
        }
    }
    
    /*
     * See namespace docs
     */
    function setSelection(startNode, startIndex, endNode, endIndex, updateCursor) {
        
        debug.assert(!startNode || (startNode && typeof(startIndex) == "number"));
        debug.assert(!endNode || (endNode && typeof(endIndex) == "number"));
        
        // Should the selection be cleared?
        if (!startNode) {
            de.selection.clear(updateCursor); // Fires selection changed
            return;
        }
        
        // If the start and end node/index is the same, then nullify the end point.
        if (startNode == endNode && startIndex == endIndex) 
            endNode = null;
        
        // See if selection needs updating
        if (startNode == selStartNode && startIndex == selStartIndex &&
        ((!endNode && !selEndNode) || (endNode == selEndNode && endIndex == selEndIndex)))             
            return;
        
        // Clear selection highlight if any
        unHighlightSelection();
        
        // Set selection model (all virtual)
        selStartNode = startNode;
        selStartIndex = startIndex;
        selEndNode = endNode;
        selEndIndex = endIndex;

        // If the selection range has an end point then highlight it,
        // and adjust the range to exclude any protected nodes
        if (selEndNode) {
            
            var startOccursFirst = doesStartOccurFirst(),
                procContainer;
            
            // Adjust selection start/end to exclude protected nodes
            while(selStartNode) {
                procContainer = de.doc.getProtectedNodeContainer(selStartNode);
                if (procContainer) {
                    selStartNode = (startOccursFirst ? procContainer.nextSibling : procContainer.previousSibling);
                    if (selStartNode)
                        selStartIndex = startOccursFirst ? 0 : _nodeLength(selStartNode, 1);
                } else break;
            }
            while(selEndNode) {
                procContainer = de.doc.getProtectedNodeContainer(selEndNode);
                if (procContainer) {
                    selEndNode = (startOccursFirst ? procContainer.previousSibling : procContainer.nextSibling);
                    if (selEndNode)
                        selEndIndex = startOccursFirst ? _nodeLength(selEndNode, 1) : 0;
                } else break;
            }
            
            var isValid = selStartNode && selEndNode;
            if (isValid) {
                
                isValid = false; // Verify valid range
                
                // Relocate protected nodes if they fall into the range
                var relocateProcContainers = [];
                _visitAllNodes(_getCommonAncestor(selStartNode, selEndNode), selStartNode, startOccursFirst, function(domNode){
                
                    procContainer = de.doc.getProtectedNodeContainer(domNode);
                    
                    debug.assert(!procContainer || (procContainer && (domNode != selStartNode && domNode != selEndNode)));
                    
                    if (procContainer) relocateProcContainers.push(procContainer);
                    
                    // Stop the traversal when reached the selection end node
                    if (domNode == selEndNode) {
                        isValid = true; // Flag as valid
                        return false;
                    }
                });
            }
            
            if (!isValid) {
                //debug.println("WARNING: Attempt to set selection range within a protected node");
                de.selection.clear(updateCursor);
                return;
            }

            // Relocate protected containers so they are outside of the selection
            for (var i in relocateProcContainers) {
                var pc = relocateProcContainers[i];
                if (pc.parentNode) // TODO: AND NOT DOCUMENT_FRAGMENT_NODE ??
                    pc.parentNode.removeChild(pc);
            }
            
            for (var i in relocateProcContainers) {
                var pc = relocateProcContainers[i];
                if (!pc.parentNode)
                    docBody.appendChild(pc);
            }

            // Visually highlight range            
            highlightSelection();

        }
        
        // Should the cursor be adjusted to be placed at the start/end of the new range?
        if (updateCursor !== false) {
            
            var newCursor = null;
            
            // Only set cursor if the range is editable
            if (de.selection.isRangeEditable()) {

                var ani = selEndNode ? 
                    getActualNodeIndex(selEndNode, selEndIndex) :
                    getActualNodeIndex(selStartNode, selStartIndex);
                
                // Adjust index if at end of text run
                var isRightOf = false;
                if (ani.node.nodeType == Node.TEXT_NODE && ani.index >= _nodeLength(ani.node)) {
                    isRightOf = true;
                    ani.index--;
                }
                    
                //newCursor = de.cursor.createCursorDesc(ani.node, ani.index, isRightOf);
                newCursor = de.cursor.getNearestCursorDesc(ani.node, ani.index, isRightOf, true);
            }
            
            // Set the cursor
            settingCursor = true;
            de.cursor.setCursor(newCursor);
            settingCursor = false;
            
        }
        
        // Fire selection ended event
        if (!supressSelectionEvents)
            de.selection.fireEvent("SelectionChanged");

            
    }
    
    /**
     * @param {de.cursor.CursorDescriptor} cDesc A cursor descrptor
     * @return {Number} The selection index of the given cursor.
     */
    function getSelectionIndexFromCDesc(cDesc){
        var index = cDesc.relIndex;
        if (cDesc.isRightOf && cDesc.domNode.nodeType == Node.TEXT_NODE) 
            index ++;
        return index;
    }
    
    /**
     * @return {Boolean} True if the selection start occurs before the selection end.
     */
    function doesStartOccurFirst() {
        if (!selEndNode) 
            return true;
            
        if (selStartNode == selEndNode) 
            return selStartIndex < selEndIndex;

        // Convert sel start/end virtual range to actual dom nodes
        var actualStart = getActualNodeIndex(selStartNode, selStartIndex).node,
            actualEnd = getActualNodeIndex(selEndNode, selEndIndex).node;
        
        var startOccursFirst = false;
        _visitAllNodes(docBody, actualStart, true, function(domNode){
            startOccursFirst = (domNode == actualEnd);
            return !startOccursFirst;
        });
        
        return startOccursFirst;
    }
    
    
    /**
     * @namespace
     * Cross-browser DEdit-specific selection. Implemented as a continuous selection model.
     */
    de.selection = {
    
        /**
         * Sets a new selection.
         * 
         * @param {Node} startNode      The starting dom node of the selection range.
         *
         * @param {Number} startIndex 	The inclusive start index in the start node.
         * 								Ranges from 0 to the text length for text nodes.
         *                              Where 0 indicates that the range begins at the first char, and text length
         *                              indicates that the range begins directly after the text node, but not including it.
         *                              <br>
         *                              Ranges from 0 to 1 for elements.
         *                              Where 0 indicates that the range includes the element and its decendants,
         *                              and 1 indicates that the range excludes the element and its decendants.
         *
         *
         * @param {Node} endNode        The ending dom node of the selection range.
         *
         * @param {Number} endIndex 	The inclusive end index in the end node.
         * 								Ranges from 0 to the text length for text nodes.
         *                              Where 0 indicates that the range ends at the first char, and text length
         *                              indicates that the range ends directly after the text node, but not including it.
         *                              <br>
         *                              Ranges from 0 to 1 for elements.
         *                              Where 0 indicates that the range includes the element and its decendants,
         *                              and 1 indicates that the range excludes the element and its decendants.
         */
        setSelection: setSelection,
        
        /**
         * Clears any current selection in the document.
         * Implementaion Note: If there is highlighting, the the DOM will be manipulated and the cursor will be
         * updated.
         * @param {Boolean} updateCursor (optional) False to supress updating the cursor. Otherwise will destroy the current cursor.
         */
        clear: function(updateCursor){

            // Restore any highlighting
            unHighlightSelection();
           // Nullify range
           selStartNode = selEndNode = null;
           
            // Update the cursor
            if (updateCursor !== false) {
                settingCursor = true;
                de.cursor.setCursor(null);
                settingCursor = false;
            }
                
            if (!supressSelectionEvents)
                de.selection.fireEvent("SelectionChanged");
            
        },
        
        /**
         * Retreives the selection range.
         * 
         * @param {Boolean} inOrder True to get the range in left-to-right traversal order, 
         *                          False to get the range as it is (i.e. the selection end may physically appear before the selection start).
         * 
         * @return {Object} The current selections range in the document. Null if there is none.
         *         The selection range will have the following members:
         *         <br>
         *         startNode - the dom node of the beginning of the selection
         *         <br>
         *         startIndex - the index of the beginning of the selection. 
         *         <br>
         *         endNode - the dom node of the end of the selection.
         *         May not be present - if not, then the selection does not range for more than one charactor/element
         *         (i.e. no highlighting present).
         *         <br>
         *         endIndex - the index of the end of the selection. 
         *         May not be present - if not, then the selection does not range for more than one charactor/element
         *         (i.e. no highlighting present).
         *         <br>
         *         inOrder - True if the start tuple occurs before the end tuple wrt in-order traversal.
         *                
         */
        getRange: function(inOrder) {
            
            if (!selStartNode) return null;
            
            if (selEndNode) { // Is there selection ranging beyond one charactor/element?
 
                var startOccursFirst = doesStartOccurFirst();
                 
                // Determine whether the start point occurs before the end point
                var declareStartFirst = !inOrder || startOccursFirst;
                
                
                range = {
                    inOrder : inOrder || (startOccursFirst == declareStartFirst),
                    startNode: declareStartFirst ? selStartNode : selEndNode,
                    startIndex: declareStartFirst ? selStartIndex : selEndIndex,
                    endNode: declareStartFirst ? selEndNode : selStartNode,
                    endIndex: declareStartFirst ? selEndIndex : selStartIndex
                };
    
            } else {
                range = {
                    startNode: selStartNode,
                    startIndex: selStartIndex
                };   
            }
            
            return range;

        }, 
        
        /**
         * @return {Boolean} True if there is selection which is all editable. False if there is no selection,
         *                   or the selection contains non-editable content.
         */
        isRangeEditable : function() {
           
           if (selStartNode) {
               var actualStart = getActualNodeIndex(selStartNode, selStartIndex).node;
               var actualEnd = selEndNode ? getActualNodeIndex(selEndNode, selEndIndex).node : actualStart;
               var ca = _getCommonAncestor(actualStart, actualEnd, true);
               return (actualStart != actualEnd && de.doc.isEditSection(ca)) || de.doc.isNodeEditable(ca);
           }
           
           return false;
        },
        
        /**
         * @return {String}  "highlight" if there is a highlighted selection,
         *                   "single" if their is only a cursor present (no highlighted range)
         *                   Null if there is nothing selected.
         */
        getState : function() {
            if (highlightFragment)
                return "range";
            if (selStartNode)
                return "single";
            return null;
        },
        
        /**
         * Converts the given node index into an actual tuple.
         * 
         * An actual tuple is a node and index which is valid while selectoin highlighting is present.
         * (Selection highlighting can create new nodes).
         * 
         * @param {Node} node    The node to convert
         * 
         * @param {Number} index The index to convert
         * 
         * @return {Object}      An node/index tuple of the actual values.
         */
        getActualNodeIndex : getActualNodeIndex,
        
        /**
         * Converts the given node index into a virtual tuple.
         * 
         * A virtual tuple is a node and index which is valid when all selection highlighting is removed.
         * (Selection highlighting can create new nodes).
         * 
         * @param {Node} node    The node to convert
         * 
         * @param {Number} index The index to convert
         * 
         * @return {Object}      An node/index tuple of the virtual values.
         * 
         */
        getVirtualNodeIndex : getVirtualNodeIndex,
        
        
        setHightlightCSS: function(){
        
            // TODO..
        
        },
        
        
        /**
         * Removes the current selection from the document (which is undoable - automatically added to undo manager).
         * @return {Boolean}   True if there was something selected and therefore removed. 
         *                     False if there was nothing to remove.
         *                     
         * @throws Error if range is not editable.
         * 
         * @see de.selection.isRangeEditable
         */
        remove : function() {
            
            var selRange = this.getRange(true);
            
            if (selRange && selRange.endNode) {
                
                if (!this.isRangeEditable())
                    _error("Attempt to remove selection which contains uneditable content");
                
                // Execute the removal action
                de.UndoMan.execute(
                    "RemoveDOM", 
                    selRange.startNode, 
                    selRange.startIndex, 
                    selRange.endNode, 
                    selRange.endIndex);

                return true;
            }
            
            return false;
            
        },
        
        /**
         * @return {Node}  A copy of the selection in the document if there is anything highlighted.
         *                 Null if there is nothing highlighted.
         */
        getHighlightedDOM : function() {
  
            // Is there anything highlighted?
            if (highlightFragment) {
                
                // Get rid of highlight CSS
                _undoOperations(formatOpList);
                
                var highlightRoot = (function trav(frag){
                
                    // Shallow copy the fragment's dom node
                    var clonedNode = frag.node.cloneNode(false);
                    
                    // Recurse into fragments children and build up cloned dom tree
                    for (var i in frag.children) {
                        var child = trav(frag.children[i]);
                        clonedNode.appendChild(child);
                    }
                    
                    return clonedNode;
                    
                })(highlightFragment);
                
                // Restore highlight CSS
                _redoOperations(formatOpList);
                
                return highlightRoot;
                
            }
            
            return null;
        },
        
        /**
         * Selects all content in an editable section.
         * 
         * @param {Node} targetES (Optional) An editable selection to select. If not provided the
         *                        current cursor's editable section owner will be selected.
         */
        selectAll: function(targetES) {
            
            // If target editable section not provided then get ES at current cursor position
            if (!targetES) {
                var cDesc = de.cursor.getCurrentCursorDesc();
                if (cDesc) 
                    targetES = de.doc.getEditSectionContainer(cDesc.domNode);
            }
            
            // Anything to select?
            if (targetES)  {
                
                de.selection.setSelection(
                    targetES.firstChild, 
                    0, 
                    targetES.lastChild,
                    _nodeLength(targetES.lastChild, 1));
           }
        },
        
        /**
         * Selects the start or end of an editable sectoin
         * @param {Element} esEle  An editable sectoin
         * @param {Boolean} start  True to select the beginning of the editable section, false for the end
         */
        selectES : function(esEle, start) {
            
            debug.assert(de.doc.isEditSection(esEle));
            
            var deepNode = esEle;
            if (start) {
                while (deepNode.firstChild) {
                    deepNode = deepNode.firstChild;
                }
            } else {
                while (deepNode.lastChild) {
                    deepNode = deepNode.lastChild;
                }
            }
            
            var cDesc = de.cursor.getNearestCursorDesc(deepNode, start ? 0 : _nodeLength(deepNode, 1), !start, !start);
            
            if (cDesc)
                de.cursor.setCursor(cDesc);
        },
        
        /**
         * @param {Node} node A dome node
         * @param {Index} index An index (ranges from 0-text-length-1 for text nodes).
         * @return {Object} The range of a word at the given index - null if there is no word.
         */
        getWordRangeAt : function(node, index) {
            
            if (node.nodeType == Node.TEXT_NODE) {
                var range = {
                    startNode:node,
                    startIndex:index,
                    endNode:node,
                    endIndex:index
                };
    			
    			var checkESBoundry = de.doc.isNodeEditable(node);
                
                // Expand start backward to first occurance whitespace / block exclusive
                _visitAllNodes(docBody, node, false, function(domNode) {
                   
                   if (domNode != node && _findAncestor(domNode, _getCommonAncestor(domNode, node, true), isWordBreakElement, true))
                        return false; // Abort when break out of inline group
                   else if (domNode.nodeType == Node.TEXT_NODE) { // Scan for whitespace - extend range backward
                       for (var i = domNode == node ? index : _nodeLength(domNode) - 1; i >= 0; i--) {
                           var c = domNode.nodeValue.charAt(i);
                           //if (_isAllWhiteSpace(c) || c == _NBSP)
                           if (wordBreakerChars.test(c))
                               return false;
                           range.startNode = domNode;
                           range.startIndex = i;
                       }
                       
                   } 
                    
                });
    
                // Expand start forward to first occurance whitespace / block exclusive
                _visitAllNodes(docBody, node, true, function(domNode) {
                    
                   if (domNode != node && (isWordBreakElement(domNode) || _findAncestor(node, _getCommonAncestor(domNode, node, true), isWordBreakElement, true)))
                   	    return false; // Abort when break out of inline group
                   	    
                   if (domNode.nodeType == Node.TEXT_NODE) { // Scan for whitespace - extend range backward
                       for (var i = domNode == node ? index : 0; i < _nodeLength(domNode); i++) {
                           var c = domNode.nodeValue.charAt(i);
                           //if (_isAllWhiteSpace(c) || c == _NBSP)
                           if (wordBreakerChars.test(c))
                               return false;
                           range.endNode = domNode;
                           range.endIndex = i;
                       }
                       
                   }
                    
                }); 
                
                range.endIndex++;
                
                return range;   
                
            }
        
            function isWordBreakElement(domNode) {
                return _isBlockLevel(domNode) || wordBreakerInlines[_nodeName(domNode)] || (checkESBoundry && de.doc.isEditSection(domNode));
            }
        },
        
        /**
         * 
         * @param {[String]} formatTypes The format types to poll. Case insensitive. See format dom action for format type list
         * 
         * @return {Object} The edit state
         */
        getEditState : function(formatTypes) {
            
            var state = {
                formatStates : {},
                inlineContainerType : null,
                textAlign : null,
                blockQuote : false
            };
            
            // Is highlight? 
            if (highlightFragment) {
                
                // Remove highlight formatting
                 _undoOperations(formatOpList);
                
                // Traverse through nodes ... checking inlines for computed CSS and for block-parent types / links
                (function trav(frag){
	                updateState(frag.node);
                })(highlightFragment);
                    
                 // Restore highlight css
                 _redoOperations(formatOpList);

            }
            
            // Is there any selection - and is it a textnode/inline node?
            if (selStartNode && (selStartNode.nodeType == Node.TEXT_NODE || _isInlineLevel(selStartNode))) 
                updateState(selStartNode.nodeType == Node.TEXT_NODE ? selStartNode.parentNode : selStartNode);
            
            // Return the state
            return state;
            
            function isBlockQuote(domNode) {
                return _nodeName(domNode) == "blockquote";
            }
            
            function updateState(node) {
                
                if (_isInlineLevel(node)) {
                
                    var eProps = de.doc.getEditProperties(node) || {};
                    
                    // TODO: Format filtering here? Or at another place - i.e. via keystrokes wouldnt use this function...
                    // Could have in both places... could always do it in actions

                    
                    // Determine format states
                    for (var i in formatTypes) {
                        var fType = formatTypes[i].toLowerCase();
                        
                        // Determine formattings
                        if (!state.formatStates[fType] || state.formatStates[fType] != "mixed") {
                        
                            var current = node, wasFound = false;
                            while (current != docBody && de.doc.isNodeEditable(current)) { // for each editable ancestor up to the document body
                                // Evaluate specific format state on specific node
                                var res = _formatEnvironment[fType + "Eval"](current);
                                
                                if (res) {
                                    if (typeof state.formatStates[fType] == "undefined") 
                                        state.formatStates[fType] = res.value; // Set first time

                                    else if (state.formatStates[fType] !== res.value) state.formatStates[fType] = "mixed";
                                    
                                    wasFound = true;
                                    break;
                                }
                                current = current.parentNode;
                                
                            } // End loop: scanning ancestors formatting
                            
                            // Was the current format type found?
                            if (!wasFound)
                                state.formatStates[fType] = (state.formatStates[fType] === null || typeof state.formatStates[fType] == "undefined") ? null : "mixed";
                            
                            
                        }
                        
                    } // End loop: evaluating format states
                    
                
                }
                
                if (node.nodeType == Node.ELEMENT_NODE) {
                    
                    // Evaluate text alignment
                    if (state.textAlign != "mixed") {
                    
                        var alignment = _getComputedStyle(node, "text-align");
                        
                        if (!alignment) 
                            alignment = "start";
                        
                        // Left/Right value based on start/end is conditional - depends on if browser is LTR OR RTL 
                        if (alignment == "start") 
                            alignment = _localeDirection == "rtl" ? "right" : "left";
                        else if (alignment == "start") alignment = _localeDirection == "rtl" ? "left" : "right";
                        
                        if (!state.textAlign) 
                            state.textAlign = alignment;
                        else if (alignment != state.textAlign) state.textAlign = "mixed";
                        
                    }
                        
                    
                    // Evaluate inline container type
                    if (state.inlineContainerType != "mixed") {

                        var iCon = _isBlockLevel(node) ? node : _findAncestor(node, docBody, _isBlockLevel, true);
                        
                        if (!de.doc.isNodeEditable(iCon))
                            iCon = null;
                        
                        var cType = iCon ? _nodeName(iCon) : "none";
                        
                        if (!state.inlineContainerType) 
                            state.inlineContainerType = cType;
                            
                        else if (cType != state.inlineContainerType) 
                            state.inlineContainerType = "mixed";

                    }
                    
                    // Evaluate blockquote state
                    if (!state.blockQuote) {
                        
                        var bq = isBlockQuote(node) ? node : _findAncestor(node, docBody, isBlockQuote, true);
                        
                        if (de.doc.isNodeEditable(bq))
                            state.blockQuote = true;
                    }
                    

                }
                
                
                
            }
            
        }
        
        
    };
    
})();

	//End Selection.js
//Start Spell.js
$enqueueInit("Spell", function() {
    
    // Setup auto-selection of marked errors
    _addHandler(_engine == _Platform.GECKO ? window : document, "mouseup", function() {
        
        var sel = de.selection.getRange();
        if (sel && !sel.endNode) {
            
            // The user clicked in an editable section... i.e. did not range a selection
            var spellNode = de.spell.getMarkedAncestor(sel.startNode);
            if (spellNode) {
                
                // The user click inside inline content marked as a spelling error
                // so select the whole word
                de.selection.setSelection({
                    startNode:spellNode,
                    startIndex:0,
                    endNode:spellNode,
                    endIndex:1
                });
            }
        }
        
    });
    
}, "Selection"); // add mouseup event after selection's event registration

(function() {
    
    var 
        /**
         * The classname of the wrappers used for marking spelling mistakes
         * @final
         * @type String
         */
        SPELL_MARK_CLASS_NAME = "sw-spell-error",
        SPELL_MARK_CLASS_NAME_RE = /^sw-spell-error$/;
    
    /**
     * @namespace
     */
    de.spell = {
        
        /**
         * @param {[Element]} editableSections (Optional) The editable sections to get words from.
         *                                     If not provided then all editable sections will be queried.
         * 
         * @return {[String]} The <em>set</em> of words found in all the given editable sections.
         */
        getWords : function(editableSections) {
            
            if (!editableSections)
                editableSections = de.doc.getAllEditSections();
            
            var words = [], wmap = {};
            
            for (var i in editableSections) {
                visitWords(editableSections[i], function(word) {
                    if (!wmap[word]) { // Unique word?
                        words.push(word);
                        wmap[word] = 1; // Track words to build a set
                    }
                });
            }
            
            return words;
            
        },
        
        /**
         * Scans a given set of editable sections for spelling errors, for each spelling error found
         * it wraps them with a span with the spelling error class. 
         * 
         * Calling this may result in a undoable action executed
         * via the undo manager (where the users can undo themselves).
         * 
         * @param {[String]|String} errors An array of miss-spelt words - or a string of whitespace seporated words
         * 
         * @param {[Element]} editableSections (Optional) An array of editable sections to mark errors for.
         *                                     If not provided then all editable sections will be queried.
         */
        markWords : function(errors, editableSections) {
            
            if (!editableSections)
                editableSections = de.doc.getAllEditSections();
                
             // Convert error array/string into a lookup map
             errors = $createLookupMap(typeof errors == "string" ?  errors.replace(/\s/g,',') : errors.join(','));
             
             var foundError = 0;
             
             for (var i in editableSections) {
                visitWords(editableSections[i], function(word, startNode, startIndex, endNode, endIndex) {
                    
                    // Is this word considered an error?
                    if (errors[word]) {
                        // Wrap the word and get the fragment of the selected word.
                        // Group all the actions together as one unit.
                        var frag = de.UndoMan.execute(
                            foundError ? de.UndoMan.ExecFlag.GROUP : 0, 
                            'SpellMark',
                            startNode, 
                            startIndex, 
                            endNode, 
                            endIndex + 1);
                            
                        foundError = 1;
                        return frag.children[frag.children.length-1].node; // Return the (exclusive) resume node as the last text node that made up the word
                    }
                    
                    
                });
            }
            
        },
        
        /**
         * Clears all words marked with spelling errors in a given set of editable sections.
         * This operation will add a single undoable action to the undo manager if any
         * marked spelling errors are found.
         * 
         * @param {[Element]} editableSections (Optional) An array of editable sections to clear marked errors from.
         *                                     If not provided then all editable sections will be queried.
         *                                     
         * @return {Boolean} Evaluates true if some errors were unmarked (i.e. an undoable action was added
         *                   to the undo managaer).
         */
        clearAllMarks : function(editableSections) {
            
            if (!editableSections)
                editableSections = de.doc.getAllEditSections();
            
            var errorWrappers = [], i;
            
            for (i in editableSections) {
                
                // Find all spelling error wrappers in each editable section
                _visitAllNodes(editableSections[i], editableSections[i], true, function(domNode) {
                    if (de.spell.isSpellErrorWrapper(domNode))
                        errorWrappers.push(domNode);   
                });
            }
            
            // Remove all the wrappers
            for (i in errorWrappers) {
                de.UndoMan.execute(i == '0' ? 0 : de.UndoMan.ExecFlag.GROUP, "SpellUnmark", errorWrappers[i]);
            }
            
            // If nothing was found then zero will be returned.
            return errorWrappers.length;
            
        },
        
        /**
         * Clears a specific marked spelling error.
         * This will add an action to the undo manager.
         * 
         * @param {Element} markNode The spelling error wrapper to "ignore" (i.e. remove)
         */
        ignoreError : function(markNode, execFlags) {
            de.UndoMan.execute(execFlags ? execFlags : 0, 'SpellUnmark', markNode);
        },
        
        /**
         * Replaces an error with a correction.
         * This will add an action to the undo manager.
         * 
         * @param {Element} markNode   The spelling error wrapper to "correct" (i.e. replace)
         * @param {String} correction  The word to replace the spelling error with
         */
        correctError : function(markNode, correction) {
            de.UndoMan.execute('SpellCorrect', markNode, correction);
        },
        
        /**
         * @param {Node} node A dom node
         * @return {Element} The spelling error wrapper which is either the given dom node or a ancestor of the given dom node.
         */
        getMarkedAncestor : function(node) {
            return this.isSpellErrorWrapper(node) ? node : _findAncestor(node, this.isSpellErrorWrapper);
        },
        
        /**
         * @param {Node} node  The node to test
         * @return {Boolean}   True iff the node is a spelling error wrapper element.
         */
        isSpellErrorWrapper : function(node) {
            return _findClassName(node, SPELL_MARK_CLASS_NAME_RE) ? true : false;
        },
        
        /**
         * Strips spelling wrapper tags from HTML
         * 
         * @param {String} html HTML Markup.
         * 
         * @return {String} The given HTML with all spelling error wrappers removed.
         */
        stripSpellWrapperHTML : function(html) {
            
            var re = /<span class\s*=\s*(?:"|')sw-spell-error(?:"|')\s*>([^<]+)<\/span>/i;
                
           // TODO: Can I just use replace rather than dealing with loops all the time?
           
           // Remove spelling wrappers via RE
            while (match = re.exec(html)) {
                html = html.substr(0, match.index) + match[1] + html.substr(match.index + match[0].length);
            }

            return html;
        }
        
        
    };

    /**
     * Scans for words in an editable section.
     * 
     * @param {Element} editableSection    The editable section to scan words for
     * 
     * @param {Function} callback          The callback function to send words to. Takes 5 args:
     *                                     word (string) -         The word that was found
     *                                     startNode (Node) -      The start text node of the word
     *                                     startIndex (Number) -   The start index of the word within the start text node 
     *                                     endNode (Node) -        The end text node of the word
     *                                     endIndex (Number) -     The end index of the word within the end text node 
     *                                     
     *                                     Return the node to exclusively resume the in-order traversal from
     */
    function visitWords(editableSection, callback) {
        
        var wordBreakerChars = /^[^\w']$/,
            resumeNode = editableSection,
            startNode,
            startIndex,
            endNode,
            endIndex,
            curWord;
        
        while (resumeNode) {
            
            // Continue visiting nodes within the editable section...
            _visitAllNodes(editableSection, resumeNode, true, function(domNode) {
                
                // Skip resume node (exclusive start point)
                if (resumeNode == domNode)
                    resumeNode = 0;  // Clear current resume node
                
                else {
                    
                     // For all text nodes within an element (e.g. not within comments, styles or scripts)...
                    if (domNode.nodeType == Node.TEXT_NODE && domNode.parentNode.nodeType == Node.ELEMENT_NODE) {
                        
                        // Only allow words to extend between adjacent text nodes .. otherwise
                        // wrapping the words will become over complex and bloat the code.
                        if (endNode && domNode.previousSibling != endNode) {
                            if (checkWord())
                                return false; // NB: Will revisit this node since will exclusively resume at the previous sibling
                        }
                       
                        var str = domNode.nodeValue, i;
                       
                        // For each charactor in this text node's string
                        for (i = 0; i < str.length; i++) {
                            
                            var c = str.charAt(i);
             
                            // Is this charactor a part of a word or a word breaker?
                            if (wordBreakerChars.test(c)) {
                                
                                // If so then check for a pending word...
                                if (checkWord())
                                    return false;
                                    
                            } else { // Found word symbol
                                
                                // Set start node/index for start of new word
                                if (!startNode) {
                                    startNode = domNode;
                                    startIndex = i;
                                    curWord = "";
                                }
                                
                                // Build word
                                curWord += c;
                                
                                // Track end point
                                endNode = domNode;
                                endIndex = i;
                            }
                            
                        } // End loop: parsing words in text node
                        
                    }
                    
                }
                
            });
            
        } // End loop: visiting nodes within the given editable section

        // Check any pending word....        
        checkWord();

        /**
         * Checks if there is a pending word, if there is then the callback is invoked.
         * @return {Node} The node to exclusively resume from if the callback changed the DOM.
         */
        function checkWord() {
            
            // Is there a word pending? Exclude words in a package or within a protected heirarchy
            if (startNode 
                && endNode
                && !de.doc.isProtectedNode(startNode) 
                && !de.doc.isNodePackaged(startNode)) {
                
                // Invoke callback
                resumeNode = callback(curWord, startNode, startIndex, endNode, endIndex);
                
                // Reset start/end point.
                startNode = endNode = 0;
                
                // Return result
                return resumeNode;
            }
            
            // Reset start/end point.
            startNode = endNode = 0;
        }
        
    } // End visitWords function
    
    
    _registerAction("SpellMark", {
        
        /**
         * An undoable action: wraps a adjacent group of text nodes with a spelling error wrapper
         * 
         * @param {Node} startNode      
         * @param {Number} startIndex
         * @param {Node} endNode
         * @param {Number} endIndex
         */
        exec : function(startNode, startIndex, endNode, endIndex) {
           
            // Build a fragment - isolating the word within the text node
            var frag = _buildFragment(null, startNode, startIndex, endNode, endIndex),
                 wrapper = $createElement("span");
            
            // Wrap the text with a span
            _setClassName(wrapper, SPELL_MARK_CLASS_NAME);
            _execOp(_Operation.INSERT_NODE, wrapper, frag.node, frag.children[0].pos);
            
            for (var i = 0; i < frag.children.length; i++) {
                var migrant = frag.children[i].node;
                _execOp(_Operation.REMOVE_NODE, migrant);
                _execOp(_Operation.INSERT_NODE, migrant, wrapper);
            }
            
            return frag;
        }
        
    });
    
    _registerAction("SpellUnmark", {
    
        /**
         * Removes a wrapper
         * 
         * @param {Element} markedNode A mark wrapper element to remove...
         */
        exec : function(markedNode) {
            
            debug.assert(markedNode && de.spell.isSpellErrorWrapper(markedNode));
    
            // Decide whether to remove the wrapper
            /*var shouldRemove = _getClassName(node) != SPELL_MARK_CLASS_NAME;
            if (!shouldRemove) 
                shouldRemove = _doesHaveElementStyle(node);
                */
            
            // TODO: REFACTOR THIS OPERATION... IT IS USED EVERYWHERE
            while(markedNode.firstChild) {
                var migrant = markedNode.firstChild;
                _execOp(_Operation.REMOVE_NODE, migrant);
                _execOp(_Operation.INSERT_NODE, migrant, markedNode.parentNode, _indexInParent(markedNode));
            }
            _execOp(_Operation.REMOVE_NODE, markedNode);
            
        }
        
    });
    
    _registerAction("SpellCorrect", {
        
        /**
         * Replaces an error with a correction.
         * 
         * @param {Element} markNode   The spelling error wrapper to "correct" (i.e. replace)
         * @param {String} correction  The word to replace the spelling error with
         */
        exec : function(markedNode, correction) {
            _execOp(_Operation.INSERT_NODE, document.createTextNode(correction), markedNode.parentNode, _indexInParent(markedNode));
            _execOp(_Operation.REMOVE_NODE, markedNode);
        }
        
    });

    
})();

//End Spell.js
//Start Typing.js
de.Typing = {}; // Model

(function(){

    $enqueueInit("Typing", function() {
        _addHandler(document, "keystroke", onKeyStroke);
        _model(de.Typing);
    }, "MVC");
    
    function onKeyStroke(e, normalizedKey) {
        
        debug.println("Key-Stroke: '" + normalizedKey + "'");
        
        // Fire typing event
        var typingEvent = {cancel:false};
        de.Typing.fireEvent("Typing", typingEvent, e, normalizedKey);
    
        // Cancel event if requested
        if (typingEvent.cancel)
            return;

        // Is the CTRL (or Apple-func key for mac) down?
        if (de.events.Keyboard.isAcceleratorDown(e)) {
        
            switch (normalizedKey.toLowerCase()) {
                case "z":
                    de.UndoMan.undo();
                    return false;
                    
                case "y":
                    de.UndoMan.redo();
                    return false;
            }
            
        }

        // Don't manipulate the selection if it is not editable
        if (!de.selection.isRangeEditable() || !de.cursor.exists()) 
            return;
			
		var targetES = de.doc.getEditSectionContainer(de.cursor.getCurrentCursorDesc().domNode);
			
        // Is the CTRL (or Apple-func key for mac) down?
        if (de.events.Keyboard.isAcceleratorDown(e)) {
        
            switch (normalizedKey.toLowerCase()) {

                case "b":
                    toggleFormat("bold");
                    return false;
                    
                case "i":
                    toggleFormat("italics");
                    return false;
                    
                case "u":
                    toggleFormat("underline");
                    return false;
                    
                case "a": // Select all
                    de.selection.selectAll(targetES);
                    return false;
                
                    
            }
            
        } else if (!e.ctrlKey && !e.metaKey && !e.altKey) {
        
            var keyStr = normalizedKey;

            if (keyStr) {
            
                if (keyStr.length > 1) {
                
                    switch (keyStr) {
                        case "Space":
                            keyStr = " ";
                            break;
                        
                        case "Tab": // Indentation / promotion
                            
							if (_isBlockLevel(targetES) || targetES == docBody) {
                                
                                var firstBlock = _findAncestor(de.cursor.getCurrentCursorDesc().domNode, docBody, _isBlockLevel, 1);
                                
                                if (firstBlock && _nodeName(firstBlock) == "li") {
                                    
                                    if (e.shiftKey)
                                        de.UndoMan.execute("DemoteItem");
                                    else de.UndoMan.execute("PromoteItem");
                                    
                                } else de.UndoMan.execute("Indent", !e.shiftKey);
 
								
							}
                            
                            return false;
                            
                        case "Delete":
                        case "Backspace":
                            
                            if (de.selection.remove()) {
                                return false;
                                
                            } else {
                            
                                // Get the cursor descriptor
                                var cursorDesc = de.cursor.getCurrentCursorDesc(),
                                    preDesc,
                                    postDesc;
                                
                                if (keyStr == "Backspace") {
                                
                                    if (cursorDesc.isRightOf && (cursorDesc.placement == (de.cursor.PlacementFlag.AFTER | de.cursor.PlacementFlag.BEFORE))) {
                                        // Delete before/after nodes completely if cursor is directly after them
                                        preDesc = _clone(cursorDesc);
                                        preDesc.isRightOf = false;
                                    } else {
                                        preDesc = de.cursor.getNextCursorMovement(cursorDesc, true);
                                    }
                                    
                                    postDesc = cursorDesc;
                                    
                                } else {
                                    // Delete is just like backspace on the right charactor.
                                    preDesc = cursorDesc;
                                    
                                    if (!cursorDesc.isRightOf && (cursorDesc.placement == (de.cursor.PlacementFlag.AFTER | de.cursor.PlacementFlag.BEFORE))) {
                                        // Delete before/after nodes completely if cursor is directly after them
                                        postDesc = _clone(cursorDesc);
                                        postDesc.isRightOf = true;
                                    } else {
                                        postDesc = de.cursor.getNextCursorMovement(cursorDesc, false);
                                    }
                                    
                                }
                                
                                if (preDesc && postDesc) {
                                
                                    // Check that the range is valid
                                    var isEditable = de.doc.isNodeEditable(postDesc.domNode);
                                    if (isEditable && postDesc.domNode != preDesc.domNode) isEditable &= de.doc.isNodeEditable(preDesc.domNode);
                                    
                                    if (isEditable) {
                                    
                                        var preIndex;
                                        if (preDesc.domNode.nodeType == Node.TEXT_NODE) {
                                            preIndex = preDesc.relIndex;
                                            if (preDesc.isRightOf) preIndex++;
                                        } else {
                                        
                                            if (_nodeName(preDesc.domNode) == "br") {
                                            
                                                // If the pre desc is a line break, then count how many other line breaks there are between the pre/post desc
                                                var brCount = 0;
                                                _visitNodes(_getCommonAncestor(preDesc.domNode, postDesc.domNode), preDesc.domNode, true, null, function(domNode){
                                                    if (_nodeName(domNode) == "br") brCount++;
                                                    return domNode != postDesc.domNode;
                                                });
                                                
                                                // If there are more than one line breaks, then exclude the starting line break... since only
                                                // one line break should be removed
                                                preIndex = brCount > 1 ? 1 : 0;
                                                
                                            } else 
                                                preIndex = preDesc.isRightOf ? 1 : 0;
                                        }
                                        
                                        var postIndex;
                                        if (postDesc.domNode.nodeType == Node.TEXT_NODE) {
                                            postIndex = postDesc.relIndex;
                                            if (postDesc.isRightOf) postIndex++;
                                        } else {
                                        
                                            // If the pre desc is a line break, then count how many other line breaks there are between the pre/post desc
                                            if (_nodeName(postDesc.domNode) == "br") {
                                                var brCount = 0;
                                                _visitNodes(_getCommonAncestor(preDesc.domNode, postDesc.domNode), preDesc.domNode, true, null, function(domNode){
                                                    if (_nodeName(domNode) == "br" && !(domNode == preDesc.domNode && preIndex == 1)) brCount++;
                                                    
                                                    return domNode != postDesc.domNode;
                                                });
                                                
                                                // If there are more than one line breaks, then exclude the ending line break... since only
                                                // one line break should be removed
                                                postIndex = brCount > 1 ? 0 : 1;
                                                
                                            } else 
                                                postIndex = postDesc.isRightOf ? 1 : 0;
                                            
                                        }
                                        
                                        // Get virtual range since cursor node/index may probably be affected by the selection
                                        var preVNI = de.selection.getVirtualNodeIndex(preDesc.domNode, preIndex),
                                            postVNI = de.selection.getVirtualNodeIndex(postDesc.domNode, postIndex);
                                            
                                            
                                        // See if can use light-weight remove text action
                                        if (preVNI.node == postVNI.node && preVNI.node.nodeType == Node.TEXT_NODE) 
											de.UndoMan.execute("RemoveText", preVNI.node, preVNI.index, postVNI.index - preVNI.index);
										else {
											// Check to see that the range does not remove any editable sections
							                var ca = _getCommonAncestor(preVNI.node, postVNI.node, true);
							                if (de.doc.isEditSection(ca) || de.doc.isNodeEditable(ca)) 
												de.UndoMan.execute("RemoveDOM", preVNI.node, preVNI.index, postVNI.node, postVNI.index);
										}
                                    }
                                }
                                
                                // signal that no further event processing from further up the event stack is needed
                                return false;
                                
                            }
                                
                        case "Enter":
                        
                            de.selection.remove();
                            var cursorDesc = de.cursor.getCurrentCursorDesc();
                            debug.assert(cursorDesc != null);
                			
                            var index = cursorDesc.relIndex;
                            if (cursorDesc.domNode.nodeType == Node.TEXT_NODE) 
                                index += (cursorDesc.isRightOf ? 1 : 0);
                            else index = cursorDesc.isRightOf ? 1 : 0;
                            
							if (!de.doc.getEditProperties(targetES).singleLine) {
								if (e.shiftKey) {
								
									var lbHTML = "<br>";
									// Line breaks may need place holders too
									if (!(cursorDesc.domNode.nodeType == Node.TEXT_NODE &&
									index < _nodeLength(cursorDesc.domNode))) lbHTML += _getOuterHTML(de.doc.createMNPlaceholder());
									
									de.UndoMan.execute("InsertHTML", lbHTML, cursorDesc.domNode.parentNode, cursorDesc.domNode, index);
									
								} else if (_isBlockLevel(targetES) || targetES == docBody) {
									// Should never allow block container to be created in inline editable sections! (Invaid HTML)
									de.UndoMan.execute("SplitContainer", cursorDesc.domNode, index);
								}
							}
                            
                            return false;
                            
                        case "Home":
                        case "End":
                            
                            // Set the cursor to either the beggining or end of the current editable section.
                            
                            var isHome = keyStr == "Home", 
                                boundDesc = de.cursor.getNearestCursorDesc(targetES, isHome ? 0 : 1, !isHome, !isHome);
                            
                            if (boundDesc) {
                                de.cursor.setCursor(boundDesc);
                                de.cursor.scrollToCursor();
                                return false;
                            }
                            
                        default:
                            return true;
                    
                    }
                }
                
                // The key press is a printable charactor.
                
                // Remove any selection there might be
                de.selection.remove();
                
                // Get the cursor descriptor
                var cursorDesc = de.cursor.getCurrentCursorDesc();
                debug.assert(cursorDesc != null);

                // Calculate the cursor index
                var index = cursorDesc.relIndex;
                if (cursorDesc.domNode.nodeType == Node.TEXT_NODE && cursorDesc.isRightOf) 
                    index++;
                else if (cursorDesc.domNode.nodeType == Node.ELEMENT_NODE) 
                     index = cursorDesc.isRightOf ? 1 : 0;
    
                // Perform a text insert action via the undo manager
                de.UndoMan.execute("InsertText", cursorDesc.domNode, keyStr, index);
                
                return false;
                
            }
            
            
        }
        
    }
    
    /**
     * 
     * @param {String} formatType
     * @return {Boolean} True if something was formatted, false if not
     */
    function toggleFormat(formatType) {
        // Perform the action via the undo manager
        return de.UndoMan.execute("Format", formatType, !de.selection.getEditState([formatType]).formatStates[formatType]) ? true : false;
    }
    
    
})();

//End Typing.js
//Start TextAlingAction.js
(function() {
    
    var excludeAlignBlocks = $createLookupMap("dt,dd,caption,colgroup,col,thead,tfoot,tbody,legend,optgroup,option,area,frame");
    
    _registerAction("TextAlign", {
        
        /**
         * An undoable alignment action. Sets the text alignment for all block levels in given range.
         * Creates new containers on the fly if needed.
         * 
         * @author Brook Novak
         * 
         * @param {String} alignment    The CSS text-alignment value. Either left, right, center or justify.
         * 
         * @param {Node} startNode      (Optional) The starting dom node of the range to align.
         *                              If not provided then the current selection will be used.
         *                              If provieded must also provide endNode
         *
         * @param {Node} endNode        (Optional) The ending dom node of the range to align. Can be the same as start node
         *                              If not provided then the current selection will be used.
         *
         */
        exec : function(alignment, startNode, endNode) {
            
            // Auto-set range if not provided.
            if (!startNode) {
                
                if (!this.selBefore)
                    return; // Nothing to select
                    
                if (this.selBefore.endNode) {
                    startNode = this.selBeforeOrdered.startNode;
                    endNode = this.selBeforeOrdered.endNode;
                } else 
                    startNode = endNode = this.selBefore.startNode;
                
            }
            
            debug.assert(endNode, "Supplied start node but not the end node");

            var containers;
            
            // First check for special case: If the ranges first block level common ancestor
            // is a list item, then set alignment for the list item rather than normalizing within a list item.
            var ca = _getCommonAncestor(startNode, endNode);
            for (var level = 0; level < 2; level++) {
                while (ca != docBody && !_isBlockLevel(ca)) {
                    ca = ca.parentNode;
                }
                
                if (ca == docBody)
                    break;
                    
                if (_nodeName(ca) == "li") {
                    containers = [ca];
                    break;
                }
                ca = ca.parentNode;
            }
            
            if (!containers) 
                // Normalize containers in range and get list of all the containers
                containers = _getNormalizedContainerRange(startNode, endNode);
            
            for (var i in containers) {
                // NOTES: CSS 2+ spec allows text-align style to be applied to all block level elements.
                _visitAllNodes(containers[i], containers[i], true, function(domNode) {
                   if (_isBlockLevel(domNode) && !excludeAlignBlocks[_nodeName(domNode)]) {
                       _execOp(_Operation.SET_CSS_STYLE, domNode, "textAlign",  alignment);
                   }
                });
            }
            
            this.selAfter = this.selBefore;
        }
    });
        
})();

//end TextAlingAction.js
//start  * file: SplitContainerAction.js
(function() {
    
    var duplicateBlockMap = $createLookupMap("p,pre,h1,h2,h3,h4,h5,h6,li,address");
    
    _registerAction("SplitContainer", {
        /**
         * An undoable action. Splits a block level element's or body node's contents into two.
         * Used for creating new block level elements like paragraphs, headings and list items.
         * 
         * @author Brook Novak
         * 
         * @param {Node} domNode	The text or element node to split the (splitable) container owner into two.
         *                             
         * 
         * @param {Number} index 	The index within/next to the dom node to split from, all the remaining contents INCLUDING the index
         *                          will migrate to the new block-level element.
         * 							For text nodes, this ranges from zero to the length of the text. If at the length of the text, then
         *                          the split will begin after the text node.
         * 							For other nodes, this can be zero (split just before the node) or 1 (split after the node).
         * 
         */
        exec : function(domNode, index) {
        	debug.assert(index >= 0);
        	debug.assert(
        		(domNode.nodeType == Node.TEXT_NODE && index <= domNode.nodeValue.length) || 
        		(domNode.nodeType == Node.ELEMENT_NODE && index <= 1)
        	);            
            
            // Get the range from the starting point (exclusive) to the end of the splitter
            // Get the first ancestor block level element which to split at
            var splitter = _findAncestor(domNode.parentNode, docBody, _isBlockLevel, true) || docBody,
                endRangeNode, // Will be set to a block / doc body
                endNodeIndex;


            if (!duplicateBlockMap[_nodeName(splitter)]) {
            
                // In the case where new paragraphs are created at the split point due to the splitter not
                // being a container element which should be duplicated, the nodes to migrate may not be all
                // nodes to the right of the split point within the splitter... instead it should range from the split point
                // to (and excluding) the first occurance of a block level element within the splitter container
                
                // Find the first occuring block level element from the split point onwards.. within the
                // splitter container... if any...
                _visitAllNodes(splitter, domNode, true, function(node){
                    if (_isBlockLevel(node)) endRangeNode = node;
                    return endRangeNode == null;
                });
                
                // If contains no block level elements from the split point, then set as end of splitter
                if (!endRangeNode || endRangeNode == splitter) {
                    endRangeNode = splitter;
                    endNodeIndex = 1;
                } else if (endRangeNode == domNode) 
                    // Definitly nothing to migrate
                    endRangeNode = null;
                else 
                     // Make sure the range is exclusive of the block level element
                    endNodeIndex = 0;
                
            } else {
                endRangeNode = splitter;
                endNodeIndex = 1;
            }

            var migrantFragment;
            
            if (endRangeNode) {

                // Build a migration fragment
                migrantFragment = _buildFragment(
                    splitter, 
                    domNode, 
                    index, 
                    endRangeNode, 
                    endNodeIndex);
                    
                // Check to see if anything will be removed
                var isAllShared = true;
                migrantFragment.visit(function(f) {
                    if(!f.isShared) isAllShared = false;
                    return isAllShared;
                });
                
                if (isAllShared) 
                    // If nothing will actually be migrated, then get rid of the fragment
                    migrantFragment = null;
                else migrantFragment.disconnect(); // Remove the second half from the document
            }
    
            // Create a new container
            var insertAfterNode,
                container,
                subSplitter;
                
            if (duplicateBlockMap[_nodeName(splitter)]) {
                
                // Special case with list items: If the splitter's first block level ancestor is a list item,
                // then insert of splitting within the list item, split outside to a new list item
                if (_nodeName(splitter) != "li" && splitter != docBody) {
                    var superSplitter = _findAncestor(splitter.parentNode, docBody, _isBlockLevel, true);
                    if (superSplitter && _nodeName(superSplitter) == "li")  {
                        subSplitter = splitter;
                        splitter = superSplitter;
                    }
                }

                insertAfterNode = splitter;
                container = (splitter.nodeName.charAt(0).toLowerCase() == "h" && !migrantFragment) ?
                    $createElement("p") : /* If splitting at the end of a heading, split into new empty paragraph. */
                    splitter.cloneNode(false);
    
            // Default to paragraph
            } else container = $createElement("p");
         
            // Discover where to insert the new container (If haven't already)
            if (!insertAfterNode) {
                
                if (migrantFragment) { // Insert the container before the disconnected migrants
                    var startFrag = migrantFragment.getStartFragment();
                    
                    // Was the start fragment split into two?
                    if (migrantFragment.wasStartSplit()) {
                        // Insert after the first half of the divided text node
                        insertAfterNode = startFrag.getPreSplitNode();
                        
                    } else if (startFrag.isShared) { // Still remains in the document?
                        // Insert after this then..
                        insertAfterNode = startFrag.node;
                        
                    } else { // Removed from the document?
                        
                        // Get the first shared ancestor
                        var fsa = startFrag;
                        while (!fsa.isShared) {
                            fsa = fsa.parent;
                        }
                        
                        // Does the first shared ancestor have any child nodes in the document?
                        if (fsa.node.firstChild) {
                        
                            // Does the shared ancestor have remvoed child fragments?
                            if (fsa.children.length > 0) {
                            
                                // Was the first removed child fragment removed after the first child in its parent?
                                if (fsa.children[0].pos > 0) {
                                    // If so, insert after fsa's remaining child before the start bounds
                                    insertAfterNode = fsa.node.childNodes[fsa.children[0].pos - 1];
                                } else {
                                    // If not (first frag was the first child in its dom parent), insert as first child
                                    // in the shared ancestor
                                    _execOp(_Operation.INSERT_NODE, container, fsa.node, 0);
                                    insertAfterNode = null;
                                }
                                
                            } else 
                                insertAfterNode = fsa.node.lastChild;
                            
                            
                        } else if (fsa == migrantFragment) { // Is fsa the splitter?
                            // If the start bounds where fully removed, insert container as first child of the splitter
                            _execOp(_Operation.INSERT_NODE, container, splitter, 0);
                            insertAfterNode = null;

                        } else 
                            insertAfterNode = fsa.node; // if not insert after the first shared ancestor then
                    }

                } else { // Insert the container before or after the split point
                    if (index == 0) { // Before split point
                        _execOp(_Operation.INSERT_NODE, container, domNode.parentNode, _indexInParent(domNode));
                        insertAfterNode = null;
                    } else insertAfterNode = domNode; // After split point
                }

               // Now that the insertion point is discovered from the fragments start bounds, make sure the
                // insertAfterNode's parent is block level
                if (insertAfterNode && insertAfterNode != docBody && !_isBlockLevel(insertAfterNode)) {
                    while (insertAfterNode != docBody && 
                        insertAfterNode.parentNode != docBody &&
                        !_isBlockLevel(insertAfterNode) && 
                        !_isBlockLevel(insertAfterNode.parentNode)) {
                        insertAfterNode = insertAfterNode.parentNode;
                    }
                }
                    
            }
            
            // Insert the container into the document (if haven't already done so)
            if (insertAfterNode)
                _execOp(_Operation.INSERT_NODE, container, insertAfterNode.parentNode, _indexInParent(insertAfterNode) + 1);
                
           // Migrate all of the disconnected nodes in the right-side of the split to the new container
           if (migrantFragment) {
                for (var i in migrantFragment.children) {
                    var migs = buildMigrants(migrantFragment.children[i]);
                    if (migs) 
                        _execOp(_Operation.INSERT_NODE, migs, container); // TODO: Check if migrants can validly migrate to the container?
                     // _isValidRelationship should actually be ok for Descendants/ancestors... not just immediate parents/children.
                }  
           }
    
            // Add any placeholders in the containers if need be
            if (_doesNeedMNPlaceholder(container))  // Important to check container first, it could be a child of the splitter
                _execOp(_Operation.INSERT_NODE, de.doc.createMNPlaceholder(), container);
            
            if (_doesNeedMNPlaceholder(splitter)) 
                _execOp(_Operation.INSERT_NODE, de.doc.createMNPlaceholder(), splitter);
            
            if (subSplitter && _doesNeedMNPlaceholder(subSplitter))
                _execOp(_Operation.INSERT_NODE, de.doc.createMNPlaceholder(), subSplitter);

            // Discover the new cursor position
            if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
                var cDesc = de.cursor.getNearestCursorDesc(container, 0, false, false);
                if (cDesc)
                    this.selAfter = {startNode : cDesc.domNode, startIndex : cDesc.relIndex + (cDesc.domNode.nodeType == Node.TEXT_NODE && cDesc.isRightOf ? 1 : 0)};
            }
                
            /**
             * Inner helper function
             * @param {de.dom.DOMFragment} frag
             */
            function buildMigrants(frag) {
                
                var migrantNode;
                
                if (frag.isShared) {
                    
                    // Check if all descendant fragments from this shared fragment are all shared
                    var isAllShared = true;
                    frag.visit(function(f) {
                        if(!f.isShared) isAllShared = false;
                        return isAllShared;
                    })
                    
                    // Ignore this subtree if they are all shared
                    if (isAllShared) return null;
     
                     // Clone the shared node.. since it still remains in the document...
                    migrantNode = frag.node.cloneNode(false);
                    
                } else { // disconnected
                
                    migrantNode = frag.node;
                
                    // Remove from parent and remove its children if it has any
                    if (migrantNode.parentNode && migrantNode.parentNode.nodeType != Node.DOCUMENT_FRAGMENT_NODE) 
                        _execOp(_Operation.REMOVE_NODE, migrantNode);

                    while(migrantNode.firstChild) {
                        _execOp(_Operation.REMOVE_NODE, migrantNode.firstChild);
                    }
                }
                
                // Recurse... build all children and link with the current dom node
                for (var i in frag.children) {
                    var descendants = buildMigrants(frag.children[i]);
                    if (descendants) 
                        _execOp(_Operation.INSERT_NODE, descendants, migrantNode);
                }
                
                return migrantNode;
                
            } // End inner buildMigrants            
        }
    });
    
})();
//end  SplitContainerAction.js
//start RemoveTextAction.js
_registerAction("RemoveText", {
    
    /**
     * Removes text within a single text node.
     * 
     * @param {Node} textNode    The text node to remove text from
     * 
     * @param {Number} index     The index at which to begin removing text from.
     *                           Ranges from 0 - textlength - 1
     * 
     * @param {Number} length    The amount of charactors to remove start from the given index. Must be at least 1
     */
    exec : function(textNode, index, length) {
        
    	debug.assert(index >= 0);
        debug.assert(length > 0);
    	debug.assert(textNode.nodeType == Node.TEXT_NODE);
        debug.assert((index + length) <= textNode.nodeValue.length);

        // Avoid removing text from placeholders            
        if (de.doc.isESPlaceHolder(textNode, false) || de.doc.isMNPlaceHolder(textNode, false)) {
            // Keep current cursor position
            if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
                var cDesc = de.cursor.getCurrentCursorDesc();
                if (cDesc)
                    this.selAfter = {startNode : cDesc.domNode, startIndex : cDesc.relIndex + (cDesc.domNode.nodeType == Node.TEXT_NODE && cDesc.isRightOf ? 1 : 0)};
            }
            return;
        }
        

        // Check for surrounding whitespace
        var convertLeftCA, convertRightCA;
        if (index == 0) {
            
            // Look for preceeding whitespace
            _visitAllNodes(docBody, textNode, false, function(domNode) {
                
                if (domNode == textNode) return;
                
                var ca = _getCommonAncestor(textNode, domNode);
                
                if (_findAncestor(domNode, ca, _isBlockLevel, true))
                    return false;
                    
                if (domNode.nodeType == Node.TEXT_NODE && _nodeLength(domNode) > 0) {
                    if (_isAllWhiteSpace(domNode.nodeValue.charAt(_nodeLength(domNode)-1))) {
                        convertLeftCA = ca;
                        return false;
                    }
                }
            });
            
        } else {
            if(_isAllWhiteSpace(textNode.nodeValue.charAt(index-1)))
                convertLeftCA = textNode;
        }
        
        if ((index + length) == _nodeLength(textNode)) {
            
            // Look for proceeding whitespace
            _visitAllNodes(docBody, textNode, true, function(domNode) {
                
                if (domNode == textNode) return;
                
                var ca = _getCommonAncestor(textNode, domNode);
                
                if (_isBlockLevel(domNode) || _findAncestor(textNode, ca, _isBlockLevel, true))
                    return false;
                    
                if (_nodeLength(domNode, 0) > 0) {
                    if (_isAllWhiteSpace(domNode.nodeValue.charAt(0))) {
                        convertRightCA = ca;
                        return false;
                    }
                }
            });
        } else {
            if (_isAllWhiteSpace(textNode.nodeValue.charAt(index + length)))
                convertRightCA = textNode;
        }
        
        var convertTarget = (convertLeftCA && convertRightCA && convertLeftCA != convertRightCA) ?
                            _getCommonAncestor(convertLeftCA, convertRightCA, true) : convertLeftCA || convertRightCA;

        if (convertTarget)
            _convertWSToNBSP(convertTarget);
        
        // Remove the text
        _execOp(_Operation.REMOVE_TEXT, textNode, index, length);
        
        // Normalize converted white space
        if (convertTarget) 
            _normalizeNBSP(convertTarget);
        
        // See if need to add a placeholder
        var ph, phParent = de.doc.getEditSectionContainer(textNode);

        if (phParent && _doesNeedESPlaceholder(phParent)) 
			ph = de.doc.createESPlaceholder(phParent);
		else {
			phParent = _findAncestor(textNode, docBody, _isBlockLevel, true) || docBody;
			if (_doesNeedMNPlaceholder(phParent))
				ph = de.doc.createMNPlaceholder();
		}

        // Add the needed placeholder
        if (ph)
            _execOp(_Operation.INSERT_NODE, ph, phParent);

        // Update the selection if requested
        if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
            var cDesc = ph ? de.cursor.createCursorDesc(ph, 0, false) : 
                                   de.cursor.getNearestCursorDesc(textNode, index == 0 ? 0 : index-1, index > 0, false);
            if (cDesc)
                this.selAfter = {startNode : cDesc.domNode, startIndex : cDesc.relIndex + (cDesc.domNode.nodeType == Node.TEXT_NODE && cDesc.isRightOf ? 1 : 0)};
        }
        
        // If the text node is left without text then get rid of it
        if (_nodeLength(textNode) == 0)
            _execOp(_Operation.REMOVE_NODE, textNode);

    }
});
// end RemoveTextAction.js
// start RemoveDOMAction.js
_registerAction("RemoveDOM",{


    /**
     * @class
     * An undoable dom action. Removes a given range from the document.
     * 
     * @author Brook Novak
     * 
     * @param {Node} startNode      The starting dom node of the fragments range.
     *
     * @param {Number} startIndex 	The inclusive start index in the start node.
     * 								Ranges from 0 to the text length for text nodes.
     *                              Where 0 indicates that the range begins at the first char, and text length
     *                              indicates that the range begins directly after the text node, but not including it.
     *                              <br>
     *                              Ranges from 0 to 1 for elements.
     *                              Where 0 indicates that the range includes the element and it's decendants,
     *                              and 1 indicates that the range excludes the element and it's decendants.
     *                              
     *
     * @param {Node} endNode        The ending dom node of the fragments range.
     *
     * @param {Number} endIndex 	The inclusive end index in the end node.
     * 								Ranges from 0 to the text length for text nodes.
     *                              Where 0 indicates that the range ends just before the text node, but not including it, 
     *                              and text length indicates that the range ends at the last charactor in the text run.
     *                              <br>
     *                              Ranges from 0 to 1 for elements.
     *                              Where 0 indicates that the range excludes the element and it's decendants,
     *                              and 1 indicates that the range includes the element and it's decendants.
     * 
     */        
    exec : function(startNode, startIndex, endNode, endIndex) {

        var fragmentRoot = _buildFragment(_getCommonAncestor(startNode, endNode, false), 
                startNode, startIndex, endNode, endIndex);

        // Convert all whitespaces to non-breaking spaces
        _convertWSToNBSP(fragmentRoot.node);
        
        // Collapse the range
        var fMigrantNode = fragmentRoot.collapse();
        
        // Normalize any NBSP entities
        _normalizeNBSP(fragmentRoot.node);
        
        if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
            
            var newCursorPos;
        
            // If there was a migrant, get the cursor descriptor from this
            if (fMigrantNode) 
                newCursorPos = de.cursor.getNearestCursorDesc(fMigrantNode, 0, false, true);
            else {
                // Get the deepest node on the starting bounds that still resides in the document
                var fragment = fragmentRoot.getStartFragment();
                while (fragment.node != docBody && !_isAncestor(docBody, fragment.node)) {
                    fragment = fragment.parent;
                }
                var domNode = fragment.node;
                
                // Check to see if there are any nodes in the start-bounds place
                if (fragment.children.length > 0 && domNode.childNodes.length > 0 &&
                    fragment.children[0].pos <= domNode.childNodes.length) {
                
                    if (fragment.children[0].pos == 0) {
                        newCursorPos = de.cursor.getNearestCursorDesc(domNode.firstChild, 0, false, false);
                    } else {
                        var cNode = domNode.childNodes[fragment.children[0].pos - 1];
                        newCursorPos = de.cursor.getNearestCursorDesc(cNode, _nodeLength(cNode, 2) - 1, true, false);
                    }
                } else  newCursorPos = de.cursor.getNearestCursorDesc(fragment.node, _nodeLength(fragment.node, 2) - 1, true, true);
            }
            
            if (newCursorPos)
                this.selAfter = { 
                    startNode : newCursorPos.domNode, 
                    startIndex : newCursorPos.relIndex + (newCursorPos.domNode.nodeType == Node.TEXT_NODE && newCursorPos.isRightOf ? 1 : 0)
                };
        }
    }
    
});


_registerAction("RemoveNode",{
    /**
     * A simple undoable action. Removes a dom node from the document.
     * Selection will always be cleared afterwards.
     * 
     * @param {Node} node The node to remove
     * 
     */
    exec: function(node) {
        _execOp(_Operation.REMOVE_NODE, node);
    }
});
// end RemoveDOMAction.js
//start PromoteItemAction.js
_registerAction("PromoteItem", {
    
    exec : function(startNode, endNode) {
        
        var t = this;

        // Auto-set range if not provided.
        if (!startNode) {
            
            if (!t.selBefore)
                return; // Nothing to promote
                
            if (t.selBefore.endNode) {
                startNode = t.selBeforeOrdered.startNode;
                endNode = t.selBeforeOrdered.endNode;
            } else 
                startNode = endNode = t.selBefore.startNode;
            
        }
        
        debug.assert(endNode, "Supplied start node but not the end node");

        var ca = _findAncestor(_getCommonAncestor(startNode, endNode, 1), docBody, function(testNode) {
            var nn = _nodeName(testNode);
            return nn == "li" || nn == "ol" || nn == "ul";
        }, 1);

        // Found a list item / list container in the given range?
        if (ca) {
            
            // Get all list items in the given range
            var listItems = [];
            if (_nodeName(ca) == "li") 
                listItems.push(ca);
            else {
                _visitAllNodes(ca, startNode, true, function(domNode){
                    if (_nodeName(domNode) == "li") listItems.push(domNode);
                    return domNode != endNode;
                });
                // The traversal above will skip the start node's li
                var li = _findAncestor(startNode, ca, function(testNode) {return _nodeName(testNode) == "li";}, 1);
                if (li)
                    listItems.push(li);
            }
            
            // Promote LI: Top => down
            for (var i in listItems) {
                promoteLI(listItems[i]);
            }
            
        }
        
        t.selAfter = t.selBefore;

        function promoteLI(li) {
            
            // Can this list item be promoted? It must have a list item at the same level preceeding it
            var prevLICon = li.previousSibling, nextLICon = li.nextSibling;
            while (prevLICon && prevLICon.nodeType == Node.TEXT_NODE) {
                prevLICon = prevLICon.previousSibling;
            }
            while (nextLICon && nextLICon.nodeType == Node.TEXT_NODE) {
                nextLICon = nextLICon.nextSibling;
            }
            
            var subList;
            
            if (prevLICon && (_nodeName(prevLICon) == "ol" || _nodeName(prevLICon) == "ul")) // Add to end of exisiting sb-list within previous LI?
                subList = prevLICon;
            else {
                // Create a new LI->[OL/UL] and insert it before the LI to promote
                subList = li.parentNode.cloneNode(false);
                _execOp(_Operation.INSERT_NODE, subList, li.parentNode, _indexInParent(li));
            }
            
            // Move the LI into the sublist
            _execOp(_Operation.REMOVE_NODE, li);
            _execOp(_Operation.INSERT_NODE, li, subList);
            
            // Check if need to merge follow list items
            if (nextLICon && _nodeName(nextLICon) == _nodeName(subList)) {

                // If so then merge the list containers

                // Merge the lists
                _execOp(_Operation.REMOVE_NODE, nextLICon);
                while(nextLICon.firstChild) {
                    var migrant = nextLICon.firstChild;
                    _execOp(_Operation.REMOVE_NODE, migrant);
                    _execOp(_Operation.INSERT_NODE, migrant, subList);
                }

            }
        }
        
        
    }
});

// end PromoteItemAction.js
//start ModifyTableAction.js
(function() {
    
    _registerAction("ModifyTable", {
        
        /**
         * An undoable action. Provides a range of table modification operations
         * 
         * @author Brook Novak
         * 
         * @param {String} op           The operation name (case insensitive). Can be: 
         *                                  insert-rows-before-n,  Where n is the amount
         *                                  insert-rows-after-n,  Where n is the amount
         *                                  delete-rows,
         *                                  insert-cols-before-n,  Where n is the amount
         *                                  insert-cols-after-n,  Where n is the amount
         *                                  delete-cols,
         *                                  merge-cells,
         *                                  split-cells,
         *                                  delete-table                  
         * 
         * @param {Node} startNode      The starting dom node of the range to align.
         *
         * @param {Node} endNode        The ending dom node of the range to align. Can be the same as start node
         * 
         * @throws {Error}              If given operation is unknown/malformed.
         *
         */
        exec : function(op, startNode, endNode) {
            
            var tableNode = null,
                selectedNodes = [];
                
            // Find first occurance of a table (it does not make sense to modify multiple tables)
            _visitAllNodes(_getCommonAncestor(startNode, endNode), startNode, true, function(domNode) {
                
                // Find table
                if (!tableNode)
                   tableNode = _findAncestor(domNode, docBody, function(node){ return (_nodeName(node) == "table"); }, true);
               
                // Record selected nodes
                selectedNodes.push(domNode);
                
                return domNode != endNode; // Traverse only in range
            });
            
            // Is a table selected?
            if (tableNode) {
                
                var tableSelection = {
                    start : null, /* [row-index,col-index,node] */
                    end : null /* [row-index,col-index,node] */
                };
                
                // Get selection within table
                for (var y = 0; y < tableNode.rows.length; y++) { // For each row
                    var row = tableNode.rows[y];
                    for (var x = 0; x < row.cells.length; x++) { // For each cell within row
                        var cell = row.cells[x];
                        
                        // Determine if this cell is selected
                        for (var i in selectedNodes) {
                            if (selectedNodes[i] == cell || _isAncestor(cell, selectedNodes[i])) {
                                
                                // Update table selection info
                                if (!tableSelection.start) 
                                    tableSelection.start = [parseInt(y),parseInt(x), cell];
                                tableSelection.end = [parseInt(y),parseInt(x), cell];
                                break;
                            }
                        }
                    }
                }
                
                if (tableSelection.start) { // Is anything in the table selection / does have content to modify?
                    
                    // Determine table operation
                    op = op.toLowerCase().split('-');
                    
                    switch(op[0]) {
                        case "insert":
                            
                            if (op.length == 4) {
                                
                                // Determine amount of rows / cols to insert
                                var amount = parseInt(op[3]);
                                if (isNaN(amount))
                                    _error(_ErrorCode.BAD_ARGS);
                                
                                // Insert the rows/cols
                                if (op[1] == "rows")
                                    insertRows(tableNode, tableSelection, op[2] == "before", amount);
                                else if (op[1] == "cols") 
                                    insertCols(tableNode, tableSelection, op[2] == "before", amount);
     
                                else _error(_ErrorCode.BAD_ARGS);
                                
                            }
    
                            else _error(_ErrorCode.BAD_ARGS);
                        
                            break;
                            
                        case "delete":
                            
                            if (op.length != 2) 
                                _error(_ErrorCode.BAD_ARGS);
                            
                            switch(op[1]) {
                                case "table":
                                    deleteTable(tableNode);
                                    break;
                                    
                                 case "rows":
                                    deleteRows(tableNode, tableSelection);
                                    break;
                                    
                                 case "cols":
                                    deleteCols(tableNode, tableSelection);
                                    break;
                                    
                                 default:
                                    _error(_ErrorCode.BAD_ARGS);
                            }
                            
                            break;
                        
                        case "merge":
                            break;
                            
                        case "split":
                            break;
                        
                        default:
                            _error(_ErrorCode.BAD_ARGS);
                    }
                    
                    
                    
                    // TODO: UPDATE CURSOR
                
                }
                
                
            }
            

        }
    });
    
    /**
     * Inserts a new table cell containing a placeholder.
     * @param {Node} row      The tr element to insert a row into
     * @param {Number} index  The index of the cell to insert.
     */
    function insertCell(row, index) {
        
        // Insert a new cell
        var newCell = _execOp(_Operation.INSERT_CELL, row, index);
        
        // Add placeholder markup
        de.UndoMan.execute(
            de.UndoMan.ExecFlag.DONT_STORE, 
            "InsertHTML", "<p>" + _getOuterHTML(de.doc.createMNPlaceholder()) + "</p>", 
            newCell);
    }
    
    /**
     * Inserts one or more rows into a table
     * @param {Node} table        The table to insert rows into
     * @param {Object} selection  The table selection
     * @param {Boolean} isBefore  True to insert before selection, false after
     * @param {Number} amount     The amount of rows to add
     */
    function insertRows(table, selection, isBefore, amount) {
        
        // Determine insertion index
        var newRowIndex;
        if (isBefore) 
            newRowIndex = selection.start[0];
        else { // insert after - must consider row-span
            
            newRowIndex = selection.end[0] + 1;
            var rspan = selection.end[2].getAttribute("rowspan");
            if (rspan) newRowIndex += (parseInt(rspan)-1);
        }
        
        // Clamp
        if (newRowIndex > table.rows.length)
            newRowIndex = table.rows.length;
        
        // Determine column count for create cols per new row
        var columnCount = getColumnCount(table);
                                
        for (var i = 0; i < amount; i++) {

            // Create the new row
            var newRow = _execOp(_Operation.INSERT_ROW, table, newRowIndex);
            
            // Insert empty cells to fill out row
            for (var j = 0; j < columnCount; j++) {
                insertCell(newRow, 0);
            }
            
        }
    }
    
    /**
     * 
     * @param {Object} selection    The table selection
     * @param {Boolean} isBefore    True to get column index before selection, false after
     * @return {Number}             The column index before/after the given selection
     */
    function getColumnIndex(selection, isBefore) {
        
         var index;
        
        if (isBefore) {
            index = Math.min(selection.start[1], selection.end[1]);
        }  else { // insert after - must consider col-span
            
            var startIndex = selection.start[1],
                endIndex = selection.end[1];
            
            var cspan = selection.start[2].getAttribute("colspan");
            if (cspan) startIndex += (parseInt(cspan)-1);
            
            cspan = selection.end[2].getAttribute("colspan");
            if (cspan) endIndex += (parseInt(cspan)-1);
            
            index = Math.max(startIndex, endIndex);
        }
        
        return index;
    }
    
    /**
     * @param {Node} table     The table to check
     * @return {Number}        The amount of columns in the given table
     */
    function getColumnCount(table) {
        var columnCount = 0;
        for (var y = 0; y < table.rows.length; y++) { 
            columnCount = Math.max(columnCount, table.rows[y].cells.length);
        }
        return columnCount;
    }
    
    /**
     * Inserts one or more columns into a table
     * @param {Node} table        The table to insert columns into
     * @param {Object} selection  The table selection
     * @param {Boolean} isBefore  True to insert before selection, false after
     * @param {Number} amount     The amount of columns to add
     */
    function insertCols(table, selection, isBefore, amount) {
        
        // Determine column insertion index
        var newColIndex = getColumnIndex(selection, isBefore);
        if (!isBefore)
            newColIndex++; // Insert AFTER
        
        for (var i = 0; i < amount; i++) {
            
            for (var y = 0; y < table.rows.length; y++) { // For each row in table
                var row = table.rows[y];
                
                // Determine insert index ... may not be newColIndex due to colspans
                var spannedIndex = -1,
                    insertIndex = newColIndex;
                    
                for (var x = 0; x < row.cells.length; x++) { // For each cell in current row
                    var cell = row.cells[x];
                    
                    spannedIndex ++;
                    
                    // Add column span if has span length more than 1
                    var cspan = cell.getAttribute("colspan");
                    if (cspan) {
                        spannedIndex += (parseInt(cspan) - 1);
                    }
                    
                    // Break if found insert index
                    if (spannedIndex >= newColIndex) {
                        insertIndex = parseInt(x);
                        break;
                    }
                } // End loop: each cell
                
                // Insert the new cell which makes up the new column in this row
                insertCell(row, insertIndex);
                
            } // End loop: each row
        
        } // End loop: insertion amount
        
    }
    
    /**
     * Deletes an entire table
     * @param {Node} table The table element to delete
     */
    function deleteTable(table) {
        _execOp(_Operation.REMOVE_NODE, table);
    }
    
    /**
     * Deletes all selected rows in table. 
     * Deletes entire table if all rows selected.
     * 
     * @param {Node} table        The table element to delete rows from
     * @param {Object} selection  The table selection
     */
    function deleteRows(table, selection) {
        
        // Is all table selected?
        if (selection.start[0] == 0 && selection.end[0] >= (table.rows.length-1)) 
            deleteTable(table);
            
        else {
            for (var i = 0; i <= (selection.end[0] - selection.start[0]); i++) {
                _execOp(_Operation.DELETE_ROW, table, selection.start[0]);
            }
        }
    }
    
    /**
     * Deletes all selected columns in table. 
     * Deletes entire table if all columns selected.
     * 
     * @param {Node} table        The table element to delete columns from
     * @param {Object} selection  The table selection
     */
    function deleteCols(table, selection) {
        
        var colStartIndex = getColumnIndex(selection, true),
            colEndIndex = getColumnIndex(selection, false),
            columnCount = getColumnCount(table);
  
        // Check if need to delete entire table
        if (colStartIndex == 0 && colEndIndex >= (columnCount-1))
            deleteTable(table);
            
        else {
            for (var i = 0; i <= (colEndIndex - colStartIndex); i++) {
                
                for (var y = 0; y < table.rows.length; y++) { // For each row in table
                    var row = table.rows[y];
                    
                    // Determine deletion index ... may not be colStartIndex due to colspans
                    var spannedIndex = -1,
                        deleteIndex = colStartIndex;
                        
                    for (var x = 0; x < row.cells.length; x++) { // For each cell in current row
                        var cell = row.cells[x];
                        
                        spannedIndex ++;
                        
                        // Add column span if has span length more than 1
                        var cspan = cell.getAttribute("colspan");
                        if (cspan) {
                            spannedIndex += (parseInt(cspan) - 1);
                        }
                        
                        // Break if found insert index
                        if (spannedIndex >= colStartIndex) {
                            deleteIndex = parseInt(x);
                            break;
                        }
                    } // End loop: each cell
                    
                    // Delete the cell
                    _execOp(_Operation.DELETE_CELL, row, deleteIndex);
                    
                } // End loop: each row               
                
            } // End loop: deleting columns
        }
    }
    
    
    
    
    function mergeCells(table, selection) {
        
        /*
        var mergeWidth = (selection.end[1] - selection.start[1]) + 1,
            mergeHeight = (selection.end[0] - selection.start[0]) + 1,
            mergeTarget = ?;
        
        for (var y = 0; y < mergeHeight; y++) { // Merge all adjacent cells in y-axis (colums)
            
            // Merge this row into merge target
            for (var x = 1; x < mergeWidth; x++) { // Merge row into target
                
                _execOp(_Operation.DELETE_CELL, table, selection.start[0]);
                
                x += colspanextra;
                
            }
        }*/
        
        
    }
    
    
    function splitCells(selectedNodes) {
        
        for (var i in selectedNodes) {
            var node = selectedNodes[i];
            
            if (_nodeName(node) == "td" || _nodeName(node) == "th") { // Is cell?
            
               // Does this selected cell have a column span?
               var span = node.getAttribute("colspan");
               if (span)
                   _execOp(_Operation.SET_ATTRIBUTE, node, "colspan", ""); // Erase span

               var span = node.getAttribute("rowspan");
               if (span) 
                   _execOp(_Operation.SET_ATTRIBUTE, node, "rowspan", ""); // Erase span
            }
            
        }
    }
    
        
})();
//End ModifyTableAction.js
//Start ItemizeAction.js 
(function() {
    
    /* 
     * Lookup map containing element names which when itemized should be converted to a list item rather than being
     * a descendant of the list items.
     */
    var convertContainers = $createLookupMap("p, div");
    
    _registerAction("Itemize", {
            
        /**
         * An undoable itemize action. Creates/converts/destroys a list of items in a given range
         * 
         * @author Brook Novak
         * 
         * 
         * @param {Boolean} bullets    	True to itemize as bullet list, false to itemize as numbered list.
         * 
         * @param {Node} startNode      (Optional) The starting dom node of the range to align.
         *                              If not provided then the current selection will be used.
         *                              If provieded must also provide endNode
         *
         * @param {Node} endNode        (Optional) The ending dom node of the range to align. Can be the same as start node
         *                              If not provided then the current selection will be used.
         *
         */
        exec : function(bullets, startNode, endNode) {
            
            // Auto-set range if not provided.
            if (!startNode) {
                
                if (!this.selBefore)
                    return; // Nothing to select
                    
                if (this.selBefore.endNode) {
                    startNode = this.selBeforeOrdered.startNode;
                    endNode = this.selBeforeOrdered.endNode;
                } else 
                    startNode = endNode = this.selBefore.startNode;
                
            }
            
            debug.assert(endNode, "Supplied start node but not the end node");
            
            // Perfom execute (recursive operation)
            (function exec(start, end, destroy, normalizedRange) {

                // Get master container
                var masterContainer = _findAncestor(
                    _getCommonAncestor(start, end, true),
                    docBody,
                    _isBlockLevel,
                    true
                ) || docBody, destroyListItems, itemizeContainers;
                
                // Deepen start/end range
                var deepStart = start, deepEnd = end;
                while (deepStart.firstChild) {
                    deepStart = deepStart.firstChild;
                }
                while (deepEnd.lastChild) {
                    deepEnd = deepEnd.lastChild;
                }
                
                // Handle special case 1: range within tables
                if (_isTableElement(masterContainer) && 
                    _nodeName(masterContainer) != "td" && 
                    _nodeName(masterContainer) != "th") {
                    
                    // Get all the table's cells within the range
                    var tabCells = [];
                    _visitAllNodes(masterContainer, masterContainer, true, function(domNode) {
                        if (domNode.nodeType == Node.ELEMENT_NODE && 
                          (_nodeName(domNode) == "td" || _nodeName(domNode) == "th")) {
                           tabCells.push(domNode);
                           return (domNode != deepEnd && !_isAncestor(domNode, deepEnd));
                       }
                       return domNode != deepEnd;
                    });
                   
                    // Check if all cells contains nothing but list items and normalize their contents
                    var areAllListEles = true, normalizedRanges = [];
                    for (var i in tabCells) {
                        var res = areAllListElements(tabCells[i], tabCells[i]);
                        if (res) {
                            areAllListEles &= res.allListEles;
                            normalizedRanges.push(res.nrange);
                        } else normalizedRanges.push(0);
                    }
                    
                    // Recurse on table cell
                    for (var i in tabCells) {
                        if (normalizedRanges[i])
                            exec(tabCells[i], tabCells[i], areAllListEles, normalizedRanges[i]);
                    }
                    
                    return;
                }
                
                // If master container is a list container itself, adjust master container to its parent
                if (_nodeName(masterContainer) == "ul" || _nodeName(masterContainer) == "ol") 
                    masterContainer = masterContainer.parentNode;
                
                // Handle special case 2: range within a list item
                var ca = masterContainer;
                for (var level = 0; level < 2 && ca; level++) {
                    if (_nodeName(ca) == "li") {
                        
                        // Determine if need to destroy list item or convert it to a different type
                        if ((normalizedRange && destroy) ||
                        (!normalizedRange &&
                        ((bullets && _nodeName(ca.parentNode) == "ul") ||
                        (!bullets && _nodeName(ca.parentNode) == "ol")))) {
                            destroyListItems = [ca]; // the list item is the target type
                        } else {
                           itemizeContainers = [ca];
                           masterContainer = ca.parentNode;
                        }
                        break;
                        
                    }
                    ca = _findAncestor(ca.parentNode, docBody, _isBlockLevel, true);
                }
                
                // Check if all containers in range are list-items/list-containers
                if (!normalizedRange && !destroyListItems && !itemizeContainers) {
                    var res = areAllListElements(start, end);
                    
                    if (res) {
                    
                        normalizedRange = res.nrange;
                        
                        // If the range contains all list elements then destroy them
                        if (res.allListEles) 
                            destroyListItems = normalizedRange;
                        else 
                            itemizeContainers = normalizedRange;
                        
                    } else 
                        return;
                    
                } else if (normalizedRange) { 
                    // If recursing on a table cell then the destroy/create operation will be predetermined
                    if (destroy)
                        destroyListItems = normalizedRange;
                    else 
                        itemizeContainers = normalizedRange;
                }
                
                if (destroyListItems) {
                    
                    // Tear down all list-items / list-containers.
                    for (var i in destroyListItems) {
                        
                        var cont = destroyListItems[i];
                        var contName = _nodeName(cont);
                        
                        if (contName == "li") {
                            convertLI(cont, true);
                            
                        } else if (contName == "ul" || contName == "ol") {
                            
                            // Determine range of list items within list container
                            var li = _isAncestor(cont, start) ? _findAncestor(start, cont) : cont.firstChild,
                                endLI = _isAncestor(cont, end) ? _findAncestor(end, cont) : cont.lastChild;
                                
                            while (li) {
                               var removeLI = li;
                               li = li == endLI ? null : li.nextSibling;
                               if (_nodeName(removeLI) == "li") // ensure not white space
                                   convertLI(removeLI, true);
                            } // End loop: destroying range of list items in list container
                            
                        }
                        
                    } // End loop: destroying list items
                    
                } else { // Creation of list items
                
                    debug.assert(itemizeContainers ? true : false);
                
                    // Create the new list container
                    var listContainer;
                    
                    // Itemize all containers in range
                    for (var i in itemizeContainers) {
                        
                        var cont = itemizeContainers[i];
                        var cRoot = cont == masterContainer ? cont : _findAncestor(cont, masterContainer),
                            contName = _nodeName(cont);
                            
                        if (contName == "li") {
                            
                            if (i == '0') {
                                listContainer = convertLI(cont, false);
                            } else {

                                var liParent = cont.parentNode;
                                _execOp(_Operation.REMOVE_NODE, cont);
                                _execOp(_Operation.INSERT_NODE, cont, listContainer);
                                
                                // If the list items container is left without any other list items, remove it
                                var child = liParent.firstChild;
                                while (child && _nodeName(child) != "li") {
                                    child = child.nextSibling;
                                }
                                
                                if (!child)
                                    _execOp(_Operation.REMOVE_NODE, liParent);
                            }
                            
                        } else {
                        
                            if (i == '0') {
                                
                                listContainer = $createElement(bullets ? "ul" : "ol"); 
                                
                                // Determine where to insert the list container
                                if (contName == "ul" || contName == "ol") {
                                    
                                    var shouldInsertBefore = false;
                                    
                                    var firstLI = cont.firstChild;
                                    while (firstLI && _nodeName(firstLI) != "li") {
                                        shouldInsertBefore |= (firstLI == start); // possible start node is white space
                                        firstLI = firstLI.nextSibling;
                                    }
                                    
                                    if (firstLI)
                                        shouldInsertBefore |= _isAncestor(firstLI, start);
                                    
                                    // Insert the list container before this list container if
                                    // the range starts at the first list item.
                                    _execOp(_Operation.INSERT_NODE, listContainer, cRoot.parentNode, _indexInParent(cRoot) + (shouldInsertBefore ? 0 : 1));
                                    
                                        
                                } else 
                                    _execOp(_Operation.INSERT_NODE, listContainer, cRoot.parentNode, _indexInParent(cRoot) + 1);

                            }
                            
                            if (contName == "ul" || contName == "ol") { // List container?
                                
                                // Migrate list items in the container within the start/end range
                                var lcChild = _isAncestor(cont, start) ? _findAncestor(start, cont) : cont.firstChild, lcEndChild = _isAncestor(cont, end) ? _findAncestor(end, cont) : cont.lastChild;
                                
                                while (lcChild) {
                                
                                    var migrateLI = _nodeName(lcChild) == "li" ? lcChild : null;
                                    lcChild = lcChild == lcEndChild ? null : lcChild.nextSibling;
                                    
                                    // NB: Keeping white space... since undo history might rely on them
                                    if (migrateLI) {

                                        // Migrate the list item into the new list item container
                                        _execOp(_Operation.REMOVE_NODE, migrateLI);
                                        _execOp(_Operation.INSERT_NODE, migrateLI, listContainer);
                                        
                                        // If the list-container is encapsulated by inline elements before the master container,
                                        // then the migrated list items children must also be encapsulated.
                                        // TODO: PHASE THIS OUT - SUPPORTS INVLAID HTML
                                        if (cRoot != cont) {
                                            var liChild = migrateLI.firstChild;
                                            while (liChild) { // For all list items immediate children
                                                if (liChild.nodeType != Node.ELEMENT_NODE && liChild.nodeType != Node.TEXT_NODE) continue;
                                                
                                                // Create a cloned sub-tree of the list containers inline parents
                                                var inode = cont.parentNode, iSubTree = [];
                                                while (inode) {
                                                    iSubTree.push(inode.cloneNode(false));
                                                    inode = inode == cRoot ? null : inode.parentNode;
                                                }
                                                
                                                // Connect up unary inline tree
                                                for (var k = iSubTree.length - 1; k > 0; k--) {
                                                    iSubTree[k].appendChild(iSubTree[k - 1]);
                                                }
                                                
                                                // Insert the cloned inline sub tree in the migrated list item
                                                // such that it encapsulated this list item child
                                                _execOp(_Operation.INSERT_NODE, iSubTree[iSubTree.length - 1], migrateLI, _indexInParent(liChild));
                                                _execOp(_Operation.REMOVE_NODE, liChild);
                                                _execOp(_Operation.INSERT_NODE, liChild, iSubTree[0]);
                                                
                                                liChild = iSubTree[iSubTree.length - 1].nextSibling;
                                            } // End loop: encapsulating list item's contents with inline elements
                                        }
                                        
                                    }
                                } // End loop: migrating list items from an existing list container to the new list container
                                // Check if the container should be removed from the document.. if
                                // it no longer contains any list items
                                var shouldRemoveCont = true;
                                lcChild = cont.firstChild;
                                while (lcChild) {
                                    if (_nodeName(lcChild) == "li") {
                                        shouldRemoveCont = false;
                                        break;
                                    }
                                    lcChild = lcChild.nextSibling;
                                }
                                
                                if (shouldRemoveCont) 
                                    _execOp(_Operation.REMOVE_NODE, cRoot); // Remove by the root
                                
                            } else {
                            
                                // Create/add a new list item
                                var listItem = $createElement("li");
                                _execOp(_Operation.INSERT_NODE, listItem, listContainer);
           
                                // Migrate contents
                                _execOp(_Operation.REMOVE_NODE, cRoot);
                                _execOp(_Operation.INSERT_NODE, cRoot, listItem);
                                
                                // Should this container element be converted into a list item? i.e.
                                // removed from within the list item it was just added to?
                                if (convertContainers[contName]) {
                                    
                                    var parent = cont.parentNode; // Might not be the list container
                                    
                                    // Remove container from the document
                                    _execOp(_Operation.REMOVE_NODE, cont);
                                    
                                    // Migrate children into the containers old parent
                                    while (cont.firstChild) {
                                        var miNode = cont.firstChild;
                                        _execOp(_Operation.REMOVE_NODE, miNode);
                                        _execOp(_Operation.INSERT_NODE, miNode, parent);
                                    }
                                    
                                }
                                
                            }
                            
                        }
                        
                    } // End loop: itemizing containes in range
                    
                }        

                /**
                 * Converts a list item, either migrates its contents up one level or changes the list item type from
                 * numbers to bullets or vice verse.
                 * 
                 * @param {Node} liEle A list item ("li") element.
                 * @param {Boolean} destroy True to migrate contents up one level, false to change type.
                 * 
                 * @return {Node} If destroy is false, it will return the new list container created for changing the list item type.
                 */
                function convertLI(liEle, destroy) {
                    
                    var sib = liEle.previousSibling, 
                        doesHavePreceedingLI = false,
                        doesHaveProceedingLI = false,
                        lcEle = liEle.parentNode,
                        didSplit = false,
                        newLCont = null;
                        
                    // Determine if the list item is preceeded with other list items
                    while(!doesHavePreceedingLI && sib) {
                        doesHavePreceedingLI |= _nodeName(sib) == "li";
                        sib = sib.previousSibling;
                    }
                    
                    // Determine if the list item is proceeded with other list items
                    sib = liEle.nextSibling
                    while(!doesHaveProceedingLI && sib) {
                        doesHaveProceedingLI |= _nodeName(sib) == "li";
                        sib = sib.nextSibling;
                    }
                    
                    if (doesHavePreceedingLI) {
                        lcEle = splitLIContainer(lcEle, liEle, !destroy, true);
                        didSplit = true;
                    }
                    
                    if (destroy) {
                    
                        // If the list item's contents are not going to be migrated within a lower level list item, 
                        // then the range needs to be normalized such that inline groups are place in paragraphs.
                        if (_nodeName(lcEle.parentNode) != "li") 
                            _getNormalizedContainerRange(liEle, liEle);
                        
                        // Migrate the list item's contents before its list container
                        while (liEle.firstChild) {
                            var liCh = liEle.firstChild;
                            _execOp(_Operation.REMOVE_NODE, liCh);
                            _execOp(_Operation.INSERT_NODE, liCh, lcEle.parentNode, _indexInParent(lcEle));
                        }
                        
                        // Remove the list item from its container
                        _execOp(_Operation.REMOVE_NODE, liEle);
                        
                    } else { // change type

                        if (didSplit) {
                            
                            if (doesHaveProceedingLI) {
                                // Get next list item element in split container
                                var nextLI = liEle.nextSibling;
                                while (_nodeName(nextLI) != "li") {
                                    nextLI = nextLI.nextSibling;
                                }
                                
                                // If previously split the container and migrated some remaining li's .. then split
                                // again on the new container - into a container of the original type.
                                splitLIContainer(lcEle, nextLI, true, true);
                            }
                                
                            newLCont = lcEle;

                        } else {
                            
                            // Insert a new container before and migrate the list item
                            newLCont = splitLIContainer(lcEle, liEle, true, false);
                            
                        }
                        
                    }
                    
                    // Check if the list item container has any list items left
                    sib = lcEle.firstChild;
                    while (sib && _nodeName(sib) != "li") {
                        sib = sib.nextSibling;
                    }
                                            
                    // If the li element's container is left without a li, remove it from the document...
                    if (!sib) {
                        
                        // Remove all ancestors of the container which do not have multiple children to keep html tidy
                        var remEle = lcEle;
                        while (remEle.parentNode.childNodes.length == 1 &&
                               !de.doc.isEditSection(remEle.parentNode) &&
                               remEle.parentNode != docBody) {
                            remEle = remEle.parentNode;
                        }
                        
                        _execOp(_Operation.REMOVE_NODE, remEle);
                        
                    }
                    
                    return newLCont;
                    
                } // End inner function: convertLI
                
                /**
                 * Splits a list item container in two. Records operation.
                 * @param {Node} lcEle          A ul or ol element
                 * @param {Node} liEle          An li element to split from within lcEle
                 * @param {Boolean} flipType    True to have the split container to have a different type than lcEle
                 * @param {Boolean} downward    True to split container from liEle downward - so new container is after lcEle.          
                 *                              False to split container from liEle upward - so new container is before lcEle.
                 *                              
                 * @return {Node} The split container.
                 */
                function splitLIContainer(lcEle, liEle, flipType, downward) {

                    // Split container in two
                    var splitC = flipType ? $createElement(_nodeName(lcEle) == "ol" ? "ul" : "ol") : lcEle.cloneNode(false);
                    
                    // Migrate this and all following li's into the split container
                    if (downward) {
                        
                        while (liEle) {
                            var migrateNode = liEle;
                            liEle = liEle.nextSibling;
                            _execOp(_Operation.REMOVE_NODE, migrateNode);
                            _execOp(_Operation.INSERT_NODE, migrateNode, splitC);
                        }
                        
                        _execOp(_Operation.INSERT_NODE, splitC, lcEle.parentNode, _indexInParent(lcEle) + 1);
                        
                    } else { // Upward
                        while (liEle) {
                            var migrateNode = liEle;
                            liEle = liEle.previousSibling;
                            _execOp(_Operation.REMOVE_NODE, migrateNode);
                            _execOp(_Operation.INSERT_NODE, migrateNode, splitC, 0);
                        }
                        
                        _execOp(_Operation.INSERT_NODE, splitC, lcEle.parentNode, _indexInParent(lcEle));
                    }

                    return splitC;
                    
                } // End inner function splitLIContainer
                

                /**
                 * Normalizes the given range and checks if all containers are list-items/list-containers of a certain type
                 * (ol or ul)
                 * 
                 * @param {Node} start    The start of the range to normalize. This range will be deepend
                 * @param {Node} end      The end of the range to normalize. This range will be deepend
                 * @return {Object}           Either Null if the range does not have containers. Or an object with
                 *                             a memeber called "allListEles" which is true iff all
                 *                             containers in the normalized range are list-items/list-containers - and
                 *                             another memeber "nrange" which contains the normalized range.
                 */
                function areAllListElements(start, end) {
                   
                    // Normalize the range
                    var normalizedRange = _getNormalizedContainerRange(start, end);
                    
                    // Anything to itemize?
                    if (normalizedRange.length == 0)
                        return null;
                        
                    // Check if range is all list elements
                    var isAllListEles = true;
                    for (var i in normalizedRange) {
                        var cname = _nodeName(normalizedRange[i]);
                        
                        // Get list item container name
                        if (cname == "li")
                            cname = _nodeName(normalizedRange[i].parentNode);
                        
                        if ((bullets && cname != "ul") || (!bullets && cname != "ol")) {
                            isAllListEles = false;
                            break;
                        }
                    }
                    
                    return {allListEles: isAllListEles, nrange: normalizedRange};
                    
                } // End inner areAllListElements
                
            })(startNode, endNode); // End recursive exec function
            
            this.selAfter = this.selBefore;
            
        } // End execute function
    });
    
})();

//End ItemizeAction.js
//Start InsertTextAction.js
_registerAction("InsertText", {
    
    exec : function(domNode, text, index) {
        
    	debug.assert(index >= 0);
        
    	debug.assert(
    		(domNode.nodeType == Node.TEXT_NODE && index <= domNode.nodeValue.length) || 
    		(domNode.nodeType == Node.ELEMENT_NODE && index <= 1)
    	);
        
        text = _parseHTMLString(_escapeTextToHTML(text)); // Escape into HTML and convert back into text with correct encoding

		var phRoot; 
        
		// If the target node is a placeholder, then the placeholder should be replaced with the new text
        if (de.doc.isESPlaceHolder(domNode, false) || de.doc.isMNPlaceHolder(domNode, false)) {
            // Get the placeholder root node
            phRoot = domNode;
            while (!(de.doc.isESPlaceHolder(phRoot, true) || de.doc.isMNPlaceHolder(phRoot, true))) {
                phRoot = phRoot.parentNode;
            }
            domNode = phRoot;
        }
		
		if (domNode.nodeType == Node.TEXT_NODE) {
            
            debug.assert(!phRoot);
			
            // Insert the text
            _execOp(_Operation.INSERT_TEXT, domNode, text, index);
			
            // Set the cursor to after the inserted text
            if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION)
                this.selAfter = {startNode : domNode, startIndex: index + text.length};
			
		} else {

			// Create a new text node and add it to the document
			var textNode = document.createTextNode(text);

            _execOp(_Operation.INSERT_NODE, textNode, domNode.parentNode, _indexInParent(domNode) + (index == 0 ? 0 : 1));
	
            // Remove the placeholder from the document if there was any
            if (phRoot) 
                _execOp(_Operation.REMOVE_NODE, phRoot);

            if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION)
                this.selAfter = {startNode : textNode, startIndex: index + _nodeLength(textNode)};
			
		}
        
        // Normalize any inserted non breaking spaces
        if (text.indexOf(_NBSP) != -1) 
            _normalizeNBSP(domNode.parentNode);

    }
});
//End InsertTextAction.js
//start InsertHTMLAction.js
_registerAction("InsertHTML", {

    /**
     * @class
     * An undoable insertion action. Inserts HTML into the document
     * 
     * @author Brook Novak
     * 
     * @param {String} html         The HTML to insert.
     * 
     * @param {Node} parentNode	    The parent node of the HTML to insert into.
     * 
     * @param {Node} refNode 	    The node to insert next to, or inside of (if a text node). 
     *                              Null if parentNode has no children, in which case the HTML will be inserted
     *                              as the only children of parentNode.
     * 
     * @param {Number} index        If refNode is null this is not applicable. 
     *                              If refNode is a Element node, then index can either be 0 or 1. 0 indicates that the
     *                              HTML should be inserted before the refNode, and 1 indicates that it should be inserted after.
     *                              Otherwise, if refNode is a Text Node, then index can range from 0 to the length of the text.
     *                              Where the index indicates that the html should be inserted before the charactor at the given index.
     *                              If index is zero, then the html will be inserted before the text node. If index is the length of the
     *                              text, then the html will be inserted after the text node. Otherwise the HTML text will be
     *                              inserted within the text run... splitting the text node in two.
     * 
     */
    exec : function(html, parentNode, refNode, index) {
       
       	debug.assert(refNode || (!refNode && parentNode.childNodes.length == 0));
		debug.assert(!refNode || index >= 0);
		debug.assert(!refNode || 
			(refNode.nodeType == Node.TEXT_NODE && index <= refNode.nodeValue.length) || 
			(refNode.nodeType != Node.TEXT_NODE && index <= 1)
		);
        
        var domRoots = [];
        
        // Get all root elements in HTML
        var domTree = parentNode == docBody ? $createElement("div") : parentNode.cloneNode(false);
        domTree.innerHTML = html;
    
        var root = domTree.firstChild;
        while(root) {
            domRoots.push(root);
            root = root.nextSibling;
        } 
        
        // Disconnect roots from temporary container
        for (var i = 0; i < domRoots.length; i++) {
            
            domTree.removeChild(domRoots[i]);
            
            var spanWrapper = 0;
            if (domRoots[i].nodeType == Node.TEXT_NODE) {
                spanWrapper = $createElement("span");
                spanWrapper.appendChild(domRoots[i]);
            }
            
            // Add a renderable char at the end of the container... this will avoid
            // consildation from changing existing contents
            var endNode = document.createTextNode("X");
            parentNode.appendChild(endNode);
            
            // Consolidate the dom root's whitespace sequences
            parentNode.appendChild(spanWrapper ? spanWrapper : domRoots[i]);
            
            _recordOperations = false;
            _consolidateWSSeqs(domRoots[i], false);
            _recordOperations = true;
            
            if (spanWrapper) {
                
                domRoots.splice(i, 1); // remove this dom root, it is to be replaced or entirly removes
                
                // Consolidation can completly remove nodes, so test each consolidated sub-tree to
                // see if they should be inserted
                if (!spanWrapper.firstChild) 
                    i --;
                    
                else {
                    // A text node can be split into several text nodes after consolidation
                    var wrappedChild;
                    while(wrappedChild = spanWrapper.firstChild) {
                        spanWrapper.removeChild(wrappedChild);
                        domRoots.splice(i, 0, wrappedChild);
                        i++;
                    }
                    i--;
                }
                
                parentNode.removeChild(spanWrapper);
                
            } else 
                parentNode.removeChild(domRoots[i]);


            parentNode.removeChild(endNode);

        }
        
		// If the target node is a placeholder, then the placeholder should be replaced with the new html
        var phRoot;
        if (de.doc.isESPlaceHolder(parentNode, false) || de.doc.isMNPlaceHolder(parentNode, false)) 
            phRoot = parentNode;
        else if (refNode && (de.doc.isESPlaceHolder(refNode, false) || de.doc.isMNPlaceHolder(refNode, false))) 
            phRoot = refNode;
            
        if (phRoot) { // Get the placeholder root and adjust the parent node / ref node 
            while (phRoot.parentNode &&
            (de.doc.isESPlaceHolder(phRoot.parentNode, false) || de.doc.isMNPlaceHolder(phRoot.parentNode, false))) {
                phRoot = phRoot.parentNode;
            }
            targetNode = phRoot;
            
            parentNode = phRoot.parentNode;
            refNode = phRoot; // See later .. it will be removed
        }
 
        var normalizeTargetWS;
        
        // Does the insertion point need to split a text node in two?
        if (refNode &&
        refNode.nodeType == Node.TEXT_NODE &&
        index > 0 &&
        index < _nodeLength(refNode)) {
            
            var rem = _execOp(_Operation.SPLIT_TEXT_NODE, refNode, index);
            
            // If inserting HTML an a whitespace point, then must convert to all NBSP then normalize later
            if (_isAllWhiteSpace(rem.nodeValue.charAt(0)) || _isAllWhiteSpace(refNode.nodeValue.charAt(index-1))) {
                normalizeTargetWS = refNode.parentNode;
                _convertWSToNBSP(normalizeTargetWS);
            }
                
        }
        
        // Get the sibling node to begin inserting the dom afterwards.
        var afterNode;
        if (!refNode) afterNode = null;
        else if (index == 0) afterNode = refNode.previousSibling;
        else afterNode = refNode;
        
        // Insert the DOM nodes
        for (var i in domRoots) {
            if (!afterNode)
                _execOp(_Operation.INSERT_NODE, domRoots[i], parentNode, parentNode.firstChild ? _indexInParent(parentNode.firstChild) : null)
            else _execOp(_Operation.INSERT_NODE, domRoots[i], parentNode, _indexInParent(afterNode) + 1);
            afterNode = domRoots[i];
        }
        
        // Normalize whitespace sequences if need to
        if (normalizeTargetWS)
            _consolidateWSSeqs(normalizeTargetWS);
        
        // Need to remove any place holders?
        if (refNode && (de.doc.isMNPlaceHolder(refNode) || de.doc.isESPlaceHolder(refNode)))
            _execOp(_Operation.REMOVE_NODE, refNode);
   
        if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
            var cDesc = de.cursor.getNearestCursorDesc(
                domRoots[domRoots.length-1],
                _nodeLength(domRoots[domRoots.length-1], 2) - 1,
                true,
                _nodeName(domRoots[domRoots.length-1]) != "br");
            if (cDesc)
                this.selAfter = {startNode : cDesc.domNode, startIndex : cDesc.relIndex + (cDesc.domNode.nodeType == Node.TEXT_NODE && cDesc.isRightOf ? 1 : 0)};
        }

    }
});

//End InsertHTMLAction.js
// Start IdenAction.js
(function() {

    /**
     * The amount of pixels to adjust margins when increasing and decreasing indents.
     * @type Number
     */
    var INDENT_WIDTH = 20,
        undentableBlocks = $createLookupMap("dt,dd,caption,colgroup,col,thead,tfoot,tbody,tr,td,th,legend,optgroup,option,area,frame");
    
    _registerAction("Indent", {
        /**
         * An undoable indentation action. Increases or decreases left indents on container elements in a given range.
         * If nodes in the given range should be indented but do not have a container, a new container is created for them.
         * 
         * @author Brook Novak
         * 
         * @param {Boolean} increase    True to increase indents in range, false to decrease indents.
         * 
         * @param {Node} startNode      (Optional) The starting dom node of the range to align.
         *                              If not provided then the current selection will be used.
         *                              If provieded must also provide endNode
         *
         * @param {Node} endNode        (Optional) The ending dom node of the range to align. Can be the same as start node
         *                              If not provided then the current selection will be used.
         * 
         *
         */
        exec : function(increase, startNode, endNode) {

            // Auto-set range if not provided.
            if (!startNode) {
                
                if (!this.selBefore)
                    return; // Nothing to select
                    
                if (this.selBefore.endNode) {
                    startNode = this.selBeforeOrdered.startNode;
                    endNode = this.selBeforeOrdered.endNode;
                } else 
                    startNode = endNode = this.selBefore.startNode;
                
            }
            
            debug.assert(endNode, "Supplied start node but not the end node");

            var containers;
            
            // First check for special case: If the ranges first block level common ancestor
            // is a list item, then indent the list item rather than normalizing within a list item.
            var ca = _getCommonAncestor(startNode, endNode);
            for (var level = 0; level < 2; level++) {
                while (ca != docBody && !_isBlockLevel(ca)) {
                    ca = ca.parentNode;
                }
                
                if (ca == docBody)
                    break;
                    
                if (_nodeName(ca) == "li") {
                    containers = [ca];
                    break;
                }
                ca = ca.parentNode;
            }
            
            if (!containers) 
                // Normalize containers in range and get list of all the containers
                containers = _getNormalizedContainerRange(startNode, endNode);
            
            // Record all indentable containers in the given range
            for (var i in containers) {
                if (!undentableBlocks[_nodeName(containers[i])]) {
    
                    var con = containers[i];
                    
                    // Determine the containers left margin in pixels
                    var marginToParse = (con.style.marginLeft && con.style.marginLeft.toLowerCase() != "auto") ?
                        con.style.marginLeft : null;
                        
                    // If margin is default/auto, get the computed style to make sure
                    if (!marginToParse)
                        marginToParse = _getComputedStyle(con, "margin-left");
                        
                    marginToParse = (marginToParse && marginToParse.toLowerCase() != "auto") ?
                        marginToParse : "0";
                    
                    // Parse intentation numberic value
                    var marginPixels;
                    if (marginToParse.indexOf("%") != -1) {
                        // Convert percentage into pixels by using the containers relative offset
                        marginPixels = con.offsetLeft;
                        
                    } else marginPixels = parseInt(marginToParse);
                    
                    var newMarginLen = marginPixels + ((increase ? 1 : -1) * INDENT_WIDTH);
                    
                    // Clip margin to zero
                    if (newMarginLen < 0) 
                        newMarginLen = 0;
                    
                    // Assign new margin
                    if (marginPixels != newMarginLen)
                        _execOp(_Operation.SET_CSS_STYLE, con, "marginLeft",  newMarginLen + "px");
                    
                }
            }
            
            this.selAfter = this.selBefore;
      
        }
    });
        
})();

//End IdentAction.js
//start FormatAction.js

_registerAction("Format", {
    
    /**
     * An undoable formatting action. Formats a range of DOM nodes in the document by
     * adjusting their CSS styles.
     * 
     * @param {String} type            The type of formatting you want to apply. Case insensitive. 
     *                                 See TODO for a list of standard formatting actions available.
     * 
     * @param {Object} value           The value to set - dependant on format type. null or empty to clear formatting.
     * 
     * @param {Object} range           (Optional) The DOM Range to apply the formatting to. If omitted then the current
     *                                 selection will be used. Must have members:
     *                                 
     * 	                               - startNode      The starting dom node of the fragments range.
     *
     *                                 - startIndex 	The inclusive start index in the start node.
     * 								        Ranges from 0 to the text length for text nodes.
     *                                      Where 0 indicates that the range begins at the first char, and text length
     *                                      indicates that the range begins directly after the text node, but not including it.
     *                                      <br>
     *                                      Ranges from 0 to 1 for elements.
     *                                      Where 0 indicates that the range includes the element and it's decendants,
     *                                      and 1 indicates that the range excludes the element and it's decendants.
     *
     *
     * 	                                - (optional) endNode        The ending dom node of the fragments range. Omit to select word at given start node/index.
     *
     * 	                                - (optional) endIndex 	The inclusive end index in the end node.  Omit to select word at given start node/index.
     * 								        Ranges from 0 to the text length for text nodes.
     *                                      Where 0 indicates that the range ends just before the text node, but not including it,
     *                                      and text length indicates that the range ends at the last charactor in the text run.
     *                                      <br>
     *                                      Ranges from 0 to 1 for elements.
     *                                      Where 0 indicates that tmFragment(frag) {
     *                                      and 1 indicates that the range includes the element and it's decendants.
     * 
     * @return {_Fragment}            The fragment which represents the range in the DOM which was formatted. Undefined if nothing formatted.
     */
    exec:function(type, value, range) {
        
        type = type.toLowerCase();
        
        // Check if the format type exists
        if (typeof _formatEnvironment[type + "Wrapper"] == "undefined") {
            debug.assert(false, 'Unknown format request: "' + type + '"');
            return;
        }

        var clear = value ? false : true, isWordRange = false, orderSelAfter = true, formatRange;

        // Auto-set range to current selectoin if none is provided
        if (!range) {
        
            if (!this.selBefore)                 
                return; // Nothing to format
            range = _clone(this.selBeforeOrdered);
            formatRange = _clone(range);
            
            // Because the range is based on the current selection, set the selection
            // after to maintain the selection before ordering (i.e. keep the cursor
            // at the same end of the selection).
            orderSelAfter = this.selBefore.inOrder;
            
        } else 
            formatRange = _clone(range);
        
        // Auto-select word-range - if the range is a single node/index tuple
        if (!formatRange.endNode) {
            
            var anchorRange = null;
            
            // Special case: instead of selecting word for links, select the whole anchor
            if (type == "link") {
                
                var current = formatRange.startNode;
                
                while (current && de.doc.isNodeEditable(current)) {
                    if (current.nodeType == Node.ELEMENT_NODE && _nodeName(current) == "a") {
                        anchorRange = {
                            startNode : current,
                            startIndex : 0,
                            endNode : current,
                            endIndex : 1 /* Since a element, 1 indicates complete right-most deepest descandant */
                        };
                        break;
                    }
                    current = current.parentNode;
                }
                
            }
            
            formatRange = anchorRange ? 
                anchorRange
                : de.selection.getWordRangeAt(formatRange.startNode, formatRange.startIndex >= _nodeLength(formatRange.startNode) ? _nodeLength(formatRange.startNode) - 1 : formatRange.startIndex);
            
            if (!formatRange)
                return; // Nothing to format
                
            isWordRange = orderSelAfter = true;
        }
        
        // Execute the action
        var fragmentRoot = formatDOMExec(
            formatRange.startNode, 
            formatRange.startIndex, 
            formatRange.endNode, 
            formatRange.endIndex, 
            _formatEnvironment[type + "Wrapper"](value || ""), 
            clear, 
            _formatEnvironment[type + "Eval"]);
            
        // Update selection if requested
        if (this.flags & de.UndoMan.ExecFlag.UPDATE_SELECTION) {
            
            if (isWordRange) {
            
                // Must get adjusted node/index since fragment will most likely of split some text nodes
                var adjustedRange = fragmentRoot.getAdjustedNodeIndex(range.startNode, range.startIndex);
      
                // Set the selection to be the single point
                this.selAfter = {
                    startNode: adjustedRange.node,
                    startIndex: adjustedRange.index
                };
            
            } else {
            
                var startFrag = fragmentRoot.getStartFragment(), endFrag = fragmentRoot.getEndFragment();
                
                var startIndex = 0, 
                endIndex = _nodeLength(endFrag.node, 1);
                
                this.selAfter = {
                    startNode: orderSelAfter ? startFrag.node : endFrag.node,
                    startIndex: orderSelAfter ? startIndex : endIndex,
                    endNode: orderSelAfter ? endFrag.node : startFrag.node,
                    endIndex: orderSelAfter ? endIndex : startIndex
                };
            }
            
        }
        
        
        /**
         * The workhorse for the format action.
         */
        function formatDOMExec(startNode, startIndex, endNode, endIndex, inlineWrapper, clear, evalElement) {
           
            var isRecursing = arguments.length > 7,
                startExclusive = startIndex == _nodeLength(startNode, 1),
                endExclusive = endIndex == 0;
                
            // Build up fragment - split any text nodes where neccessary
            var fragmentRoot = _buildFragment(_getCommonAncestor(startNode, endNode, false), 
                startNode, 
                startIndex, 
                endNode, 
                endIndex);
                
            var startFrag = fragmentRoot.getStartFragment(),
                endFrag = fragmentRoot.getEndFragment(),
                rootNode = fragmentRoot.node; // Keep track of the root node... it may change if it is removed 
                
            // Mark fragments which should not be included for formatting
            if (startExclusive)
                markFrags(startFrag);
            if (endExclusive)
                markFrags(endFrag);
        
            function markFrags(markFrag) {
                do {
                    markFrag.dontFormat = 1;
                    markFrag = markFrag.parent;
                } while(markFrag && markFrag.children.length == 1);
            }
        
            // PASS 1: Clear all related formatting
            
            // Clear all related formatting within fragment
            fragmentRoot.visit(function(frag){
                if (!frag.dontFormat)
                    removeFormatting(frag.node);
            });
            
            // Clear all related formatting up to the document root (include body since can have styles/classes)
            if (!isRecursing) {
                var domNode = rootNode; // NB: Include root in removal since it may no longer be the fragment root
                while (domNode) {
                    removeFormatting(domNode);
                    if (domNode == docBody) break;
                    domNode = domNode.parentNode;
                } // End clear formatting to to root element
            }
            
            if (!clear) { // Should new formatting be applied? I.E. Not just clearing formatting
                
                // PASS 2: Set new formatting
                
                // Rebuild fragment since structure may have changed (this won't create any extra operations)
                startNode = startFrag.node;
                endNode = endFrag.node;
                var newFragmentRoot = _buildFragment(
                    rootNode, 
                    startNode, 
                    startExclusive ? _nodeLength(startNode, 1) : 0, 
                    endNode, 
                    endExclusive ? 0 : _nodeLength(endNode, 1)
                    );
                    
                if (startExclusive)
                    markFrags(newFragmentRoot.getStartFragment());
                if (endExclusive)
                    markFrags(newFragmentRoot.getEndFragment());
                
                // NB: Keeping old fragment root in order to determine new cursor position (see below)
                
                // Traverse the fragment structure and encapsulate inline groups where needed
                (function trav(frag){
                
                    var igroups = [];
                    
                    // For all sub tress
                    for (var i in frag.children) {
                        
                        var child = frag.children[i];
           
                        if (shouldEncap(child)) 
                            igroups.push(child.node);
                        else {
                            // If cannot wrap the subtree due to containing at least one block level element, then
                            // recurse deeper
                            encapsulateIGroup(); // wrap anything pending
                            trav(child);
                        }
                    }
                    
                    // Encap remaining run if any
                    encapsulateIGroup();
                    
                    /**
                     * Checks the local igroups array and moves any nodes in the array into a wrapper
                     * then clears the array.
                     */
                    function encapsulateIGroup(){
                    
                        if (igroups.length > 0) {
                        
                            // Create inline wrapper and inset at beginning of inline group run
                            var wrapper = inlineWrapper.cloneNode(false);
                            _execOp(_Operation.INSERT_NODE, wrapper, frag.node, _indexInParent(igroups[0]));
                            
                            // Move run of inline groups into the wrapper
                            for (var i in igroups) {
                                var toEncap = igroups[i];
                                _execOp(_Operation.REMOVE_NODE, toEncap);
                                _execOp(_Operation.INSERT_NODE, toEncap, wrapper);
                            }
                            
                            // Reset igroup run
                            igroups = [];
                            
                        }
                    } // End encapsulateIGroup function
                    
                    /**
                     * @param {Node} node The node to test
                     * @return {Boolean} True iff the fragments node should be encapsulated.
                     */
                    function shouldEncap(frag) {
                        
                        // Should this fragment be excluded from formatting?
                        if (frag.dontFormat)
                            return false;
                            
                        // Check that all node inclusive don't contain block-levels 
                        var allin = true;
                        _visitAllNodes(frag.node, frag.node, true, function(domNode) {
                            allin = !_isBlockLevel(domNode);
                            return allin;
                        })
                        
                        if (allin) {
                        
                            // Last check: the fragment mimics the actual document structure.. i.e. not a partial range.
                            // This avoids encapsulating nodes which are not in the format range
                            var isStructureSame = true;
                            (function checkStructure(curFrag, curNode) {
                                
                                // Avoid including fragments which aren't actually in the format range
                                 if (curFrag.dontFormat) {
                                     isStructureSame = false;
                                     return;
                                 }
                                
                                if (curNode.firstChild) {
                                    
                                    isStructureSame = curNode.childNodes.length == curFrag.children.length;
                                    
                                    if (isStructureSame) {
                                        for (var j = 0; j < curNode.childNodes.length; j++) {
                                            checkStructure(curFrag.children[j], curNode.childNodes[j]);
                                            if (!isStructureSame)
                                                break;
                                        }
                                    }
                                    
                                } else {
                                    isStructureSame = curFrag.children.length == 0;
                                }
                                
                            })(frag, frag.node);
                            
                            return isStructureSame;
                            
                        }
                        
                        return false;
                    } // End isAllInline function
                    
                })(newFragmentRoot); // End encapsulating inline groups
                
            } // End if: not clearing formatting
            
            return fragmentRoot;
            
            /**
             * Removes immediate formatting for the given node.
             * May result in removal of node... the local range may be adjusted when removals occur
             * 
             * @param {Node} node a node to remove formatting from
             */
            function removeFormatting(node) {
                
                if (node.nodeType == Node.ELEMENT_NODE) {
                    
                    var shouldRemove = false,
                        res = evalElement(node); // Determine the actions to take to strip formatting at this element
                        
                    if (res) { // Does this element contain related formatting?
                        
                        for (var i in res.strip) {
                        
                            // Strip formatting
                            switch (res.strip[i].type) {
                                case 1: // Name match
                                    shouldRemove = true;
                                    break;
                                    
                                case 2: // Class match
                                
                                    // Remove class... but keep extra classes
                                    var cls = _getClassName(node);
                                    if (cls) { // Strip matched class
                                        var clss = cls.split(' ');
                                        for (var j in clss) {
                                            if (clss[j] == res.strip[i].match) {
                                                clss.splice(j, 1);
                                                break;
                                            }
                                        }
                                        cls = cls.length > 0 ? clss.join(' ') : "";
                                        
                                        _execOp(_Operation.SET_CLASS, node, cls);
                                    }
                                    break;
                                    
                                case 3: // Style match
                                    // Erase the matched style
                                    _execOp(_Operation.SET_CSS_STYLE, node, res.strip[i].match, "");
                                    break;
                                    
                                // @DEBUG ON
                                default:
                                    debug.assert(false, 'Unknown result type "' + res.strip[i].type + '" for stripping formatting');
                                // @DEBUG OFF
                            }
                            
                            // If going to remove then no need to strip anything.
                            if (shouldRemove) break;
                            
                        } // End loop: stripping formatting
                        
                        // Get left-most child of the current sub tree
                        var leftMost = node.firstChild;
                        while (leftMost.firstChild)
                            leftMost = leftMost.firstChild;
                            
                        // Get right-most child of the current sub tree 
                        var rightMost = node.lastChild;
                        while (rightMost.lastChild)
                            rightMost = rightMost.lastChild;
                            
                        // Check if the node should be auto-removed - only if the element is a generic span and has no class/styles.
                        if (!shouldRemove && _nodeName(node) == "span") {
                            shouldRemove = !_getClassName(node);
                            if (!shouldRemove) 
                                shouldRemove = _doesHaveElementStyle(node);
                        }
                            
                        if (shouldRemove) {
                            
                            // Remove all children and connect with parent
                            var migrations = [];
                            while (node.firstChild) {
                                var migrant = node.firstChild;
                                _execOp(_Operation.REMOVE_NODE, migrant);
                                _execOp(_Operation.INSERT_NODE, 
                                        migrant, 
                                        node.parentNode, 
                                        _indexInParent(migrations.length > 0 ? migrations[migrations.length - 1] : node) + 1
                                        );
                                migrations.push(migrant);
                            }
        
                            // If has no children, then ignore ... code wll bloat and it's too complex to bother tidying such cases
                            if (migrations.length> 0) {
        
                                // If node is the current root node then the root must be adjusted
                                if (node == rootNode)
                                    rootNode = node.parentNode;
                        
                                // Remove the actual node
                                _execOp(_Operation.REMOVE_NODE, node);
                                
                            }
        
                        }
                        
                        // Ensure that all of the elements descendants that it not within the formatting range
                        // still inherits the formatting that was just stripped using recursion
                        if (!isRecursing) {
                            
                            // Recurse on left-range?
                            var isInFormatRange = false;
                            fragmentRoot.visit(function(f){
                                isInFormatRange = (!f.dontFormat && f.node == leftMost);
                                return !isInFormatRange;
                            });
                            
                            if (!isInFormatRange) { // Do recursion
        
                                formatDOMExec(
                                    leftMost, 
                                    0, 
                                    startFrag.node, 
                                    startExclusive ? _nodeLength(startFrag.node, 1) : 0, 
                                    res.inline, 
                                    false, 
                                    evalElement, 
                                    true);
                            }
                            
                            // Recurse on right-range?
                            isInFormatRange = false;
                            fragmentRoot.visit(function(f){
                                isInFormatRange = (!f.dontFormat && f.node == rightMost);
                                return !isInFormatRange;
                            });
                            
                            if (!isInFormatRange) { // Do recursion
                                
                                formatDOMExec(
                                    endFrag.node, 
                                    endExclusive ? 0 : _nodeLength(endFrag.node, 1), 
                                    rightMost, 
                                    _nodeLength(rightMost, 1), 
                                    res.inline, 
                                    false, 
                                    evalElement, 
                                    true);
                            }
                        }
                        
                    }
                    
                }
                
            } // End inner function removeFormatting
        
        } // End inner function formatDOMExec
            
    } // End exec
});

//End FormatAction.js
//Start DemoteItemAction.js
_registerAction("DemoteItem", {
    
    exec : function(startNode, endNode) {
        
        var t = this;

        // Auto-set range if not provided.
        if (!startNode) {
            
            if (!t.selBefore)
                return; // Nothing to promote
                
            if (t.selBefore.endNode) {
                startNode = t.selBeforeOrdered.startNode;
                endNode = t.selBeforeOrdered.endNode;
            } else 
                startNode = endNode = t.selBefore.startNode;
            
        }
        
        debug.assert(endNode, "Supplied start node but not the end node");

        var ca = _findAncestor(_getCommonAncestor(startNode, endNode, 1), docBody, function(testNode) {
            var nn = _nodeName(testNode);
            return nn == "li" || nn == "ol" || nn == "ul";
        }, 1);

        // Found a list item / list container in the given range?
        if (ca) {
            
            // Get all list items in the given range
            var listItems = [];
            if (_nodeName(ca) == "li") 
                listItems.push(ca);
            else {
                _visitAllNodes(ca, startNode, true, function(domNode){
                    if (_nodeName(domNode) == "li") listItems.push(domNode);
                    return domNode != endNode;
                });
                // The traversal above will skip the start node's li
                var li = _findAncestor(startNode, ca, function(testNode) {return _nodeName(testNode) == "li";}, 1);
                if (li)
                    listItems.push(li);
            }
            
            // TODO: REFACTOR: ALL ABOVE IS IDENTICLE TO PROMOTE ACTION
            
            // Demote LI: Top => down
            for (var i in listItems) {
                demoteLI(listItems[i]);
            }
            
        }
        
        t.selAfter = t.selBefore;

        function demoteLI(li){
        
            // Can this list item be demoted? Its list container must be within a list container
            var targetLICon = li.parentNode.parentNode;
            if (_nodeName(targetLICon) == "ul" || _nodeName(targetLICon) == "ol") {
            
                // Is there a following list item at the same level
                var nextLI = li.nextSibling, prevLI = li.previousSibling; // lis, uls or ols,
                while (prevLI && prevLI.nodeType == Node.TEXT_NODE) {
                    prevLI = prevLI.previousSibling;
                }
                while (nextLI && nextLI.nodeType == Node.TEXT_NODE) {
                    nextLI = nextLI.nextSibling;
                }
                
                var insertIndex;
                
                if (nextLI && prevLI) { // List item to demote surrounded by others
                
                    // Split the contain in two
                    var lowerSplit = li.parentNode.cloneNode(false);
                    _execOp(_Operation.INSERT_NODE, lowerSplit, targetLICon, _indexInParent(li.parentNode) + 1);
                    
                    // Move following list items into split
                    while (nextLI) {
                        var migrant = nextLI;
                        nextLI = nextLI.nextSibling;
                        _execOp(_Operation.REMOVE_NODE, migrant);
                        _execOp(_Operation.INSERT_NODE, migrant, lowerSplit);
                    }
                    
                    insertIndex = _indexInParent(lowerSplit);
                    
                } else if (nextLI) {  // List item to demote contains another afterwards
                
                    insertIndex = _indexInParent(li.parentNode);
                    
                } else if (prevLI) {  // List item to demote contains another before
                
                    insertIndex = _indexInParent(li.parentNode) + 1;
                    
                } else { // List item to demote on its own
                
                    insertIndex = _indexInParent(li.parentNode);
                    
                    // Remove the container
                    _execOp(_Operation.REMOVE_NODE, li.parentNode);
                    
                }
                
                // Remove the li
                _execOp(_Operation.REMOVE_NODE, li);
                
                // Add it to the parent container
                _execOp(_Operation.INSERT_NODE, li, targetLICon, insertIndex);
                
            }
            
        }
        
    }
});


//End DemoteItemAction.js
//Start CreateTableAction.js
(function() {
    
    _registerAction("CreateTable", {

        exec : function(refNode, isBefore, rows, columns, cellspacing, cellpadding) {
            
            // TODO
            
            var markup = "<table></table>";

      
        }
    });
        
})();

//End CreateTableAction.js
//Start ChangeContainerAction.js
(function() {
    
    var changeBlockMap = $createLookupMap("p,pre,h1,h2,h3,h4,h5,h6,address");
    
    _registerAction("ChangeContainer", {
        
        /**
         * An undoable action. Changes all block level elements in a given range - creates new containers where needed.
         * 
         * @author Brook Novak
         * 
         * @param {String} containerTag (Optional) The container nodename to encapsulate the range. 
         * 
         * @param {Node} startNode      (Optional) The starting dom node of the range to align.
         *                              If not provided then the current selection will be used.
         *                              If provieded must also provide endNode
         *
         * @param {Node} endNode        (Optional) The ending dom node of the range to align. Can be the same as start node
         *                              If not provided then the current selection will be used.
         * 
         */
        exec : function(containerTag, startNode, endNode) {
			
			if (!containerTag)
				containerTag = "p";
			containerTag = containerTag.toLowerCase();
				
			debug.assert(changeBlockMap[containerTag], "Cannot change container type to \"" + changeBlockMap + "\"");
			
            // Auto-set range if not provided.
            if (!startNode) {
                
                if (!this.selBefore)
                    return; // Nothing to select
                    
                if (this.selBefore.endNode) {
                    startNode = this.selBeforeOrdered.startNode;
                    endNode = this.selBeforeOrdered.endNode;
                } else 
                    startNode = endNode = this.selBefore.startNode;
                
            }
            
            debug.assert(endNode, "Supplied start node but not the end node");
            
            
            var containers = _getNormalizedContainerRange(startNode, endNode);
			
			for (var i in containers) {
				
				var container = containers[i];
				
				// Can this block level container be changed to a certain type?
				if (_nodeName(container) != containerTag && changeBlockMap[_nodeName(container)]) {
					
					var migrants = [], 
						migrant,
						parent = container.parentNode,
						index = _indexInParent(container);
					
					// Remove container contents
					while(migrant = container.firstChild) {
						migrants.push(migrant);
						_execOp(_Operation.REMOVE_NODE, migrant);
					}
					
					// Remove container
					_execOp(_Operation.REMOVE_NODE, container);
					
					// Insert new container
					var newContainer = $createElement(containerTag);
					_execOp(_Operation.INSERT_NODE, newContainer, parent, index);
					
					// Migrate original container elements
					for (var j in migrants) {
						_execOp(_Operation.INSERT_NODE, migrants[j], newContainer);
					}
					
				}
				
			}
            
            this.selAfter = this.selBefore;

        }
		
    });
    
})();

//End ChangeContainerAction.js
	
	/* BlockQuoteAction.js */
	_registerAction("Blockquote",
			{

				/**
				 * An undoable blockquote action. Encapsulates a range with a
				 * block quote. If there is any block quotes within the range,
				 * or if the range is within a block quote, then the block quote
				 * will be removed instead.
				 * 
				 * @author Brook Novak
				 * 
				 * @param {Node}
				 *            startNode (Optional) The starting dom node of the
				 *            range to align. If not provided then the current
				 *            selection will be used. If provieded must also
				 *            provide endNode
				 * 
				 * @param {Node}
				 *            endNode (Optional) The ending dom node of the
				 *            range to align. Can be the same as start node If
				 *            not provided then the current selection will be
				 *            used.
				 * 
				 */
				exec : function(startNode, endNode) {

					// Auto-set range if not provided.
					if (!startNode) {

						if (!this.selBefore)
							return; // Nothing to select

						if (this.selBefore.endNode) {
							startNode = this.selBeforeOrdered.startNode;
							endNode = this.selBeforeOrdered.endNode;
						} else
							startNode = endNode = this.selBefore.startNode;

					}

					debug.assert(endNode,
							"Supplied start node but not the end node");

					// Is there a block quote in the given range?
					var bq = null, ca = _getCommonAncestor(startNode, endNode,
							true);

					if (isBlockQuote(ca))
						bq = ca;
					else {

						_visitAllNodes(ca, startNode, true, function(domNode) {

							if (isBlockQuote(domNode))
								bq = domNode;

							return bq == null && domNode != endNode;

						});
					}

					// Is the range inside a block quote?
					if (!bq) {
						bq = _findAncestor(ca, docBody, isBlockQuote, true)
								|| _findAncestor(startNode, ca, isBlockQuote,
										true); // Initial traversal will have
												// missed these nodes

						// Check that looking outside range was ok (not
						// venturing outside editable area)
						if (!de.doc.isNodeEditable(bq))
							bq = null;
					}

					if (bq) {

						// Move block quote children outside of block quote
						while (bq.firstChild) {
							var migrant = bq.firstChild;
							_execOp(_Operation.REMOVE_NODE, migrant);
							_execOp(_Operation.INSERT_NODE, migrant,
									bq.parentNode, _indexInParent(bq));
						}

						// Remove the block quote
						_execOp(_Operation.REMOVE_NODE, bq);

					} else { // Encapsulate range with a block quote

						// Normalize containers in range and get list of all the
						// containers
						var containers = _getNormalizedContainerRange(
								startNode, endNode);

						var newbq = $createElement("blockquote");

						if (containers.length > 0
								&& _isValidRelationship(newbq,
										containers[0].parentNode)) {

							// Add the new block quote to the document
							_execOp(_Operation.INSERT_NODE, newbq,
									containers[0].parentNode,
									_indexInParent(containers[0]));

							// Migrate containers into block quote
							for ( var i in containers) {
								var con = containers[i];
								_execOp(_Operation.REMOVE_NODE, con);
								_execOp(_Operation.INSERT_NODE, con, newbq);
							}

						}
					}

					this.selAfter = this.selBefore;

					function isBlockQuote(domNode) {
						return _nodeName(domNode) == "blockquote";
					}
				}
			});

	/* DTDUtil.js */
	var _HTML_401_MAPS = {

		/* Generic block level */
		GB : "address,blockquote,center,del,div,h1,h2,h3,h4,h5,h6,hr,ins,isindex,noscript,p,pre",

		/* Special inline level */
		SI : "a,applet,basefont,bdo,br,font,iframe,img,map,area,object,param,q,script,span,sub,sup",

		/* Phrase level */
		PH : "abbr,acronym,cite,code,del,dfn,em,ins,kbd,samp,strong,var",

		/* Font level */
		F : "b,big,i,s,small,strike,tt,u",

		/* Table elements */
		TE : "table,caption,colgroup,col,thead,tfoot,tbody,tr,td,th",

		/* Form elements */
		// FE :
		// "form,button,fieldset,legend,input,label,select,optgroup,option,textarea",
		/*
		 * Elements which to do support non-whitespace text as immediate
		 * children.
		 */
		NT : "table,textarea,tr,thead,tbody,tfoot,dl,ul,ol,menu,select,optgroup,option,script,style"
	};

	// Build maps
	for ( var mem in _HTML_401_MAPS) {
		_HTML_401_MAPS[mem] = $createLookupMap(_HTML_401_MAPS[mem]);
	}

	$extend(
			_HTML_401_MAPS,
			{
				/* Block level */
				B : $extend(
						$createLookupMap("dir,dl,fieldset,form,menu,noframes,ol,table,ul,dd,dt,frameset,li,tbody,td,tfoot,thead,th,tr"),
						_HTML_401_MAPS.GB),

				/* Inline level */
				I : $extend(
						$extend(
								$createLookupMap("abbr,acronym,cite,code,dfn,em,kbd,samp,strong,var"),
								_HTML_401_MAPS.F), _HTML_401_MAPS.SI)
			});

	/*
	 * A map of maps containing valid immediate child relationships according to
	 * HTML 4.01 transactional.
	 */
	var _HTML_401_VALIDATION_MAP = function() {

		function cloneSubset(map, exclude) {
			var arr = exclude.split(","), clone = _clone(map);
			for ( var i in arr) {
				delete clone[arr[i]];
			}
			return clone;
		}

		var BLOCKINLINEMAP = $extend(_clone(_HTML_401_MAPS.B), _HTML_401_MAPS.I);

		return {

			// Body
			body : $extend($createLookupMap("script,ins,del"), BLOCKINLINEMAP),

			// Generic block level
			address : $extend($createLookupMap("p"), BLOCKINLINEMAP),
			blockquote : BLOCKINLINEMAP,
			centre : BLOCKINLINEMAP,
			del : BLOCKINLINEMAP,
			h1 : _HTML_401_MAPS.I,
			h2 : _HTML_401_MAPS.I,
			h3 : _HTML_401_MAPS.I,
			h4 : _HTML_401_MAPS.I,
			h5 : _HTML_401_MAPS.I,
			h6 : _HTML_401_MAPS.I,
			hr : {},
			ins : BLOCKINLINEMAP,
			isindex : {},
			noscript : BLOCKINLINEMAP,
			p : _HTML_401_MAPS.I,
			pre : cloneSubset(_HTML_401_MAPS.I,
					"img,object,applet,big,small,sub,sup,font,basefont"),

			// Lists
			dir : $createLookupMap("li"),
			dl : $createLookupMap("dd,dt"),
			dt : _HTML_401_MAPS.I,
			dd : BLOCKINLINEMAP,
			li : BLOCKINLINEMAP,
			menu : $createLookupMap("li"),
			ol : $createLookupMap("li"),
			ul : $createLookupMap("li"),

			// Tables
			table : $createLookupMap("caption,col,colgroup,thead,tfoot,tbody"),
			caption : _HTML_401_MAPS.I,
			colgroup : $createLookupMap("col"),
			col : {},
			thead : $createLookupMap("tr"),
			tfoot : $createLookupMap("tr"),
			tbody : $createLookupMap("tr"),
			tr : $createLookupMap("td,th"),
			td : BLOCKINLINEMAP,
			th : BLOCKINLINEMAP,

			// Forms
			form : cloneSubset(BLOCKINLINEMAP, "form"),
			button : cloneSubset(BLOCKINLINEMAP,
					"a,input,select,textarea,label,button,iframe,form,isindex,fieldset"),
			fieldset : $extend($createLookupMap("legend"), BLOCKINLINEMAP),
			legend : _HTML_401_MAPS.I,
			input : {},
			label : cloneSubset(_HTML_401_MAPS.I, "label"),
			select : $createLookupMap("optgroup,option"),
			optgroup : $createLookupMap("option"),
			option : {},
			textarea : {},

			// Special inline elements
			a : cloneSubset(_HTML_401_MAPS.I, "a"),
			applet : $extend($createLookupMap("param"), BLOCKINLINEMAP),
			basefont : {},
			bdo : _HTML_401_MAPS.I,
			br : {},
			font : _HTML_401_MAPS.I,
			iframe : BLOCKINLINEMAP,
			img : {},
			map : $extend($createLookupMap("area"), _HTML_401_MAPS.B),
			area : {},
			object : $extend($createLookupMap("param"), BLOCKINLINEMAP),
			param : {},
			q : _HTML_401_MAPS.I,
			script : {},
			span : _HTML_401_MAPS.I,
			sub : _HTML_401_MAPS.I,
			sup : _HTML_401_MAPS.I,

			// Phrase level
			abbr : _HTML_401_MAPS.I,
			acroynm : _HTML_401_MAPS.I,
			cite : _HTML_401_MAPS.I,
			code : _HTML_401_MAPS.I,
			dfn : _HTML_401_MAPS.I,
			em : _HTML_401_MAPS.I,
			kbd : _HTML_401_MAPS.I,
			samp : _HTML_401_MAPS.I,
			strong : _HTML_401_MAPS.I,
			'var' : _HTML_401_MAPS.I,

			// Font level
			b : _HTML_401_MAPS.I,
			big : _HTML_401_MAPS.I,
			i : _HTML_401_MAPS.I,
			s : _HTML_401_MAPS.I,
			small : _HTML_401_MAPS.I,
			strike : _HTML_401_MAPS.I,
			tt : _HTML_401_MAPS.I,
			u : _HTML_401_MAPS.I

		};

	}();

	function _isNodeAtLevel(domNode, mapName) {
		if (domNode.nodeType == Node.ELEMENT_NODE)
			return _HTML_401_MAPS[mapName][_nodeName(domNode)] ? true : false;
		return false;
	}

	/**
	 * Determines if an Element or Text node can be a parent of another given
	 * Element/Text/Body node. Note: the contents of text nodes are not
	 * analysed, the relationship is valid for the type, but if the text node
	 * contains non-whitespace symbols the relationship may not be valid.
	 * 
	 * Only configured for HTML 4.01 trans .. TODO: Other specs
	 * 
	 * @see _doesTextSupportNonWS
	 * 
	 * @param {Node}
	 *            child The child to test
	 * @param {Node}
	 *            parent The parent of the child to test
	 * @return {Boolean} True if child can be a child of Parent according to
	 *         HTML 4.0 Transactional specification
	 */
	function _isValidRelationship(child, parent) {
		if (child.nodeType == Node.TEXT_NODE)
			return true;

		var vmap = _HTML_401_VALIDATION_MAP[_nodeName(parent)];
		if (vmap)
			return vmap[_nodeName(child)];

		return true; // Allow any custom nodes to be added
	}

	/*
	 * TODO: SUPPORT OF OTHER DOC TYPES.
	 */

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a block level element.
	 */
	function _isBlockLevel(domNode) {
		return _isNodeAtLevel(domNode, "B");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a block level element.
	 */
	function _isGenericBlockLevel(domNode) {
		return _isNodeAtLevel(domNode, "GB");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a inline level element.
	 */
	function _isInlineLevel(domNode) {
		return _isNodeAtLevel(domNode, "I");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a inline level element.
	 */
	function _isSpecialInlineLevel(domNode) {
		return _isNodeAtLevel(domNode, "SI");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a phrase level element.
	 */
	function _isPhraseLevel(domNode) {
		return _isNodeAtLevel(domNode, "PH");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a font level element.
	 */
	function _isFontLevel(domNode) {
		return _isNodeAtLevel(domNode, "F");
	}

	/**
	 * @param {Node}
	 *            domNode A dom node to test
	 * @return {Boolean} True iff domNode is a table element. E.G. "tr", "thead"
	 *         or "table" elements
	 */
	function _isTableElement(domNode) {
		return _isNodeAtLevel(domNode, "TE");
	}

	/*
	 * @param {Node} domNode A dom node to test @return {Boolean} True iff
	 * domNode is a form element. E.G. "input", "textarea" or "form" elements
	 */
	/*
	 * function _isFormElement(domNode) { return _isNodeAtLevel(domNode, "FE"); }
	 */

	/**
	 * @param {String}
	 *            str A string
	 * @return {Boolean} True iff str contains nothing but whitespace
	 *         charactors.
	 */
	function _isAllWhiteSpace(str) {
		return /^[\t\n\r ]+$/.test(str);
	}

	/**
	 * @param {Node}
	 *            textNode A text node which has a parent node.
	 * @return {Boolean} True iff the text node is allowed to contain non
	 *         whitespace symbols.
	 */
	function _doesTextSupportNonWS(textNode) {
		return !_isNodeAtLevel(textNode.parentNode, "NT");
	}

	/**
	 * Ready only. UTF encoding for the non breaking backspace entity
	 * ("&nbsp;")'
	 */
	var _NBSP = _parseHTMLString("&nbsp;");

	$extend(de, {
		isBlock : _isBlockLevel
	});
	/* Viewport.js */
	var _getViewportSize, _getScrollbarThickness, _getBodyOffset, _getDocumentScrollPos;

	(function() {

		var

		recalcViewportPending = 1, cachedVPWidth, cachedVPHeight;

		$enqueueInit("viewport", function() {

			// On resize invalidate scrollbars and body offset measurements
			_addHandler(window, "resize", function() {
				recalcViewportPending = 1;
			});

		});

		/**
		 * @param {Boolean}
		 *            forceReCalc True to force a relcalculation of the viewport
		 *            dimension. Otherwised it may return a cached version. Only
		 *            need to set to true if calling within a onresize event
		 *            (since cached values are re-evaluated on resize events).
		 * 
		 * @return {Object} The size of the documents viewport - excluding
		 *         scrollbars. {width, height}
		 */
		de.getViewPortSize = _getViewPortSize = function(forceRecalc) {

			if (recalcViewportPending || forceRecalc)
				reCalcViewport();

			return {
				width : cachedVPWidth,
				height : cachedVPHeight
			};

		};

		/**
		 * Recalculates the viewport dimensions
		 */
		function reCalcViewport() {

			if (_browser == _Platform.IE && _browserVersion < 7) {
				// IE 6 and below does not support fixed positioned elements and
				// has troubles with
				// 100% heights .. can just query doc element client area
				cachedVPWidth = document.documentElement.clientWidth;
				cachedVPHeight = document.documentElement.clientHeight; // TODO:
																		// For
																		// all
																		// IE's?

			} else {

				var measureDiv = $createElement("div");

				_setFullStyle(measureDiv,
						"position:fixed;top:0;left:0;width:100%;height:100%;border-style:none;margin:0");

				docBody.appendChild(measureDiv);

				cachedVPWidth = measureDiv.offsetWidth;
				cachedVPHeight = measureDiv.offsetHeight;

				docBody.removeChild(measureDiv);
			}

			recalcViewportPending = 0;

		}

	})();

	/**
	 * @return {Object} The current scroll position of the document {top, left}.
	 */
	de.getDocumentScrollPos = _getDocumentScrollPos = function() {

		var left = 0, top = 0;

		// DOM Compliant?
		if (docBody.scrollLeft || docBody.scrollTop) {

			left = docBody.scrollLeft;
			top = docBody.scrollTop;

		} else if (window.pageYOffset || window.pageXOffset) {

			left = window.pageXOffset;
			top = window.pageYOffset;

		} else if (document.documentElement.scrollLeft
				|| document.documentElement.scrollTop) {

			left = document.documentElement.scrollLeft;
			top = document.documentElement.scrollTop;

		}

		return {
			top : top,
			left : left
		};
	}

	/**
	 * Note: this does not include the browser toolbars/status bars etc.. just
	 * the document viewing area...
	 * 
	 * @return {Object} the size of the documents window - i.e. including
	 *         scrollbars. {width, height}
	 */
	function _getWindowSize() {

		var width = 0, height = 0;

		if (window.innerWidth || window.innerHeight) { // Most Browsers

			width = window.innerWidth;
			height = window.innerHeight;

		} else if (document.documentElement.offsetWidth
				|| document.documentElement.offsetWidth) { // IE

			width = document.documentElement.offsetWidth;
			height = document.documentElement.offsetHeight;

		} else if (docBody.offsetWidth || docBody.offsetWidth) {

			width = docBody.offsetWidth;
			height = docBody.offsetWidth;

		}

		return {
			width : width,
			height : height
		};
	}
	;
	/* file: WhitespaceUtil.js */
	var _consolidateWSSeqs, _normalizeNBSP, _convertWSToNBSP;
	(function() {

		/* Elements which can be physically separated by white space. */
		var breakableElements = $createLookupMap("button,img,iframe,map,object"),

		/*
		 * Inline elements which cannot be regarded as part of a whitespace
		 * sequence.
		 */
		nonWSInlineElements = $createLookupMap("br,button,img,iframe,map,object,select,textarea,applet");

		/**
		 * This does not create any undoable operations.
		 * 
		 * @param {Node}
		 *            targetNode A node to convert all whitespaces to NBSP
		 *            entities in text nodes which can support non whitespace
		 *            and has normal whitespace breaking
		 */
		_convertWSToNBSP = function(targetNode) {
			_visitTextNodes(targetNode, targetNode, true, function(textNode) {
				if (_doesTextSupportNonWS(textNode)
						&& getWSStyle(textNode) == "normal")
					textNode.nodeValue = textNode.nodeValue.replace(
							/[\t\n\r ]/g, _NBSP);
			});
		};

		/**
		 * Consolidates white space. This creates undoable operations.
		 * 
		 * @param {Node}
		 *            targetNode The DOM node and all it's descendants to
		 *            consolidate.
		 * 
		 * @param {Boolean}
		 *            extendRange If the first text node begins with whitespace,
		 *            then the first whitespace sequence may start before the
		 *            target node. If the last text node in the range ends with
		 *            whitespace, then the last whitespace sequence may end
		 *            after the target node. Set to true to allow consolidation
		 *            outside of the target node, false will truncate whitespace
		 *            sequences within the target node.
		 * 
		 */
		_consolidateWSSeqs = function(targetNode, extendRange) {

			// Get the first text node within target node - that is editable
			var ftn;
			_visitTextNodes(targetNode, targetNode, true, function(textNode) {
				if (_doesTextSupportNonWS(textNode)
						&& _nodeLength(textNode) > 0) {
					ftn = textNode;
					return false;
				}
			});

			// If there are no text nodes then there is nothing to consolidate
			if (!ftn)
				return;

			// If the first text node contains a whitespace... extend range
			// backward...
			// possibly before the targetnode... to ensure that all preceeding
			// whitespace
			// that is part of the first node/index sequence is included. May
			// over estimate but
			// that is ok.
			var currentNode = targetNode;
			var ignorePreceedingWS = false;

			if (extendRange && _isAllWhiteSpace(ftn.nodeValue.charAt(0))) {

				_visitAllNodes(null, ftn, false, function(domNode) {

					// Skip start node
					if (domNode == ftn)
						return;

					if (domNode.nodeType == Node.TEXT_NODE) {

						// Text nodes that do not support not whitespace
						// shouldn't be consolidated...
						if (!_doesTextSupportNonWS(domNode))
							return false;

						// Adjust new node to start consolidating from
						currentNode = domNode;

						// If the text node contains a nonWS charactor then the
						// range has been extended enough
						if (!_isAllWhiteSpace(domNode.nodeValue)) {
							// Set flag to ignore any preceeding whitespace at
							// the starting node (see later)
							ignorePreceedingWS = true;
							return false;
						}

					}

					// If the node is not inline, then WS sequences can't spill
					// over these
					else if (!_isInlineLevel(domNode))
						return false;

				});
			}

			var seenTargetNode = _isAncestor(targetNode, currentNode), currentIndex = 0;

			// Keep traversing through the target node's descendants until all
			// whitespace sequences are
			// consolidated or completely removed
			while (currentNode) {

				// Get the next whitespace sequence
				var seq = nextWSSequence(currentNode, currentIndex,
						ignorePreceedingWS, targetNode, seenTargetNode, false,
						false, extendRange);
				ignorePreceedingWS = false;
				seenTargetNode = seq.seenTargetNode;
				currentNode = seq.resumeNode;
				currentIndex = seq.resumeIndex;

				// Was there a whitespace sequence? if so, and the sequence is
				// not using "pre" wrapping then
				// there might be something to consolidate
				if (seq.startNode && getWSStyle(seq.startNode) != "pre") {

					// If the whitespace sequence breaks two inline/text
					// elements apart, then adjust the range
					// so that it leaves one whitespace behind
					if (isBreaker(seq.startNode, seq.startIndex, seq.endNode,
							seq.endIndex)) {

						// If the whitespace sequence is just one in length,
						// then there is nothing to consolidate
						if (seq.startNode == seq.endNode
								&& seq.startIndex == (seq.endIndex - 1)) {
							seq.startNode = null;
						} else {

							// Increment start node / index by one whitespace to
							// leave one white space behind
							// If the start index is larger/equal to the start
							// nodes text length,
							// the fragment range will include the start node,
							// but exclude it from removal.
							seq.startIndex++;
						}
					}

					// Is there anything to consolidate?
					if (seq.startNode) {

						// Create the fragment and disconnect it from the
						// document
						var seqFrag = _buildFragment(_getCommonAncestor(
								seq.startNode, seq.endNode, false),
								seq.startNode, seq.startIndex, seq.endNode,
								seq.endIndex);
						seqFrag.disconnect();

						// Keep the current node/index pointer updated
						var updateTargetNode = currentNode == targetNode;
						if (currentNode) {

							var startFrag = seqFrag.getStartFragment(), endFrag = seqFrag
									.getEndFragment(), updated = false, wasStartSplit = seqFrag
									.wasStartSplit(), wasEndSplit = seqFrag
									.wasEndSplit();

							// Is this node the same as the start node of the
							// fragment, and was the start node split?
							if (currentNode == seq.startNode && wasStartSplit) {

								debug
										.assert(startFrag.getPreSplitNode() == seq.startNode);
								debug
										.assert(_nodeLength(seq.startNode) == seq.startIndex);

								// Does the index need updating?
								if (currentIndex >= _nodeLength(seq.startNode)) {

									var remTextLen = _nodeLength(startFrag.node);

									// Does the index fall in the removed range?
									if (currentIndex < (_nodeLength(seq.startNode) + remTextLen))

										// If adjusting left, then simply
										// truncate the index to the end of the
										// start node
										currentIndex = _nodeLength(seq.startNode) - 1;

									// Was both the end node AND start node
									// split at the same node?.. and the
									// node/index
									// is pointing in the remaining text (right
									// most)?
									else if (currentNode == seq.endNode
											&& wasEndSplit) {

										// Adjust the node to become the
										// remaining text
										currentNode = endFrag
												.getPostSplitNode()

										// Set the index to become relative to
										// the split end node
										currentIndex -= (_nodeLength(seq.startNode) + remTextLen);

									} else
										assert(false);
								}

								updated = true;

								// Otherwise is this node the same as the end
								// node of the fragment, and was the end node
								// split?
							} else if (currentNode == seq.endNode
									&& wasEndSplit) {

								var remTextLen = _nodeLength(endFrag.node);

								// Does the index fall outside the removed
								// range?
								if (currentIndex >= remTextLen) {

									// Adjust the node to become the remaining
									// text
									currentNode = endFrag.getPostSplitNode();

									// Set the index to become relative to the
									// split end node
									currentIndex -= remTextLen;

									updated = true;

									// If not, then the node/index should be set
									// to the start or end bounds node/index
								} else
									currentNode = null;

							}

							if (!updated) {
								// Determine if the disconnection of the
								// fragment removed this dom node
								var wasRemoved;
								if (currentNode) {
									wasRemoved = false;
									seqFrag.visit(function(frag) {
										if (!frag.isShared
												&& frag.node == currentNode) {
											wasRemoved = true;
											return false;
										}
									});
								} else
									wasRemoved = true;

								if (wasRemoved) {

									// If the node was removed, then set the
									// node/index to the starting bounds
									var frag = startFrag;
									while (!frag.isShared) {
										frag = frag.parent;
									}

									// Set the node to become the first shared
									// node on the starting bound...
									currentNode = frag.node;

									// The index should be at the end of the
									// start bound if the very-end of start
									// bound still remains in the document,
									// Otherwise the index should be set to the
									// beggining of the start bound.
									// It is possible for the very-end of the
									// start fragment to still be included
									// because if the
									// sequence is a breaker, then the start
									// index can be incremented exclude the
									// start node.
									currentIndex = frag == startFrag ? _nodeLength(
											currentNode, 1)
											: 0;

									// If the shared node contains child nodes,
									// then set the current node to become the
									// child at which the startbounds
									// proceeded from
									if (currentNode.childNodes.length > 0
											&& frag.children.length > 0
											&& frag.children[0].pos > 0) {
										currentNode = currentNode.childNodes[frag.children[0].pos - 1];
										// Set index to the end of selected node
										currentIndex = _nodeLength(currentNode,
												1);
									}
								}
							}

							// If the current node is the same as the target
							// node, the target node
							// is a text node that has been split - so update
							// this aswell
							if (currentNode && updateTargetNode)
								targetNode = currentNode;
						}

					}

				}

			} // End loop: consolidating whitespaces in target node

		};

		/**
		 * Converts any NBSP entities within a given node (and in some cases
		 * just outside of the node) into whitespace, only if the conversion
		 * won't collapse the whitespace.
		 * 
		 * This will not create any undoable operations
		 * 
		 * @param {Node}
		 *            targetNode The node to normalize all containing non
		 *            breaking spaces
		 */
		_normalizeNBSP = function(targetNode) {

			var currentNode = targetNode, currentIndex = 0;

			while (currentNode) {

				// Get the next whitespace sequence.. including NBPS's
				var seq = nextWSSequence(currentNode, currentIndex, false,
						targetNode, true, true, true, true);
				currentNode = seq.resumeNode;
				currentIndex = seq.resumeIndex;

				// Is there a whitespace sequence?
				if (seq.startNode) {

					var isWSSeqBreaker = isBreaker(seq.startNode,
							seq.startIndex, seq.endNode, seq.endIndex), seqTextNodes = [];

					// debug.println("Found ws sequence - wordbreaker=" +
					// isWSSeqBreaker + ", endIndex=" + seq.endIndex);

					// Get all text nodes in the whitespace sequence into an
					// array
					_visitTextNodes(_getCommonAncestor(seq.startNode,
							seq.endNode, false), seq.startNode, true, function(
							textNode) {
						seqTextNodes.push(textNode);
						if (textNode == seq.endNode)
							return false;
					});

					// For each text node in the whitespace sequence....
					for (var i = 0; i < seqTextNodes.length; i++) {

						var textNode = seqTextNodes[i];

						// For each charactor in the whitespace sequence
						for (var index = (i == 0 ? seq.startIndex : 0); index < (i == (seqTextNodes.length - 1) ? seq.endIndex
								: _nodeLength(textNode)); index++) {

							// debug.println("Checking whitespace at index " +
							// index + "...");

							if (textNode.nodeValue.charAt(index) == _NBSP) {

								// debug.println("Found NBSP at index " +
								// index);

								// Keep NBSP if the NBSP is at the start or end
								// of the sequence, and the sequence is not
								// a word breaker
								if (!(!isWSSeqBreaker && ((i == 0 && index == seq.startIndex) || (i == (seqTextNodes.length - 1) && index == (seq.endIndex - 1))))) {

									// Keep the NBSP if preceded by a whitespace
									var ch;
									if (index == 0)
										ch = (i > 0) ? seqTextNodes[i - 1].nodeValue
												.charAt(_nodeLength(seqTextNodes[i - 1]) - 1)
												: null;
									else
										ch = textNode.nodeValue
												.charAt(index - 1);

									if (!ch || !_isAllWhiteSpace(ch)) {

										// Keep the NBSP if proceeded by a
										// whitespace
										if (index == (_nodeLength(textNode) - 1))
											ch = (i < (seqTextNodes.length - 1)) ? seqTextNodes[i + 1].nodeValue
													.charAt(0)
													: null;
										else
											ch = textNode.nodeValue
													.charAt(index + 1);

										// debug.println("ch = " + (ch ? ch :
										// "NULL"));

										if (!ch || !_isAllWhiteSpace(ch)) {

											// Otherwise... replace the non
											// breaking space with a whitespace

											// debug.println("Replacing NBSP at
											// index " + index + " (node length
											// = " + _nodeLength(textNode) +
											// ")");

											textNode.nodeValue = textNode.nodeValue
													.substr(0, index)
													+ " "
													+ textNode.nodeValue
															.substr(index + 1);
										}
									}

								}

							}
						} // End loop: iterating over whitespaces in ws
							// seqence
					} // End loop: Iterating over text nodes in ws sequence

				}

			} // End loop: searching for whitespace sequences in target node

		};

		/**
		 * Discovers the start and end points of the next whitespace seqeunce
		 * from a given point (inclusive)
		 * 
		 * @param {Node}
		 *            initNode The node to search from (towards the right)
		 * @param {Number}
		 *            initIndex The index to search from.
		 * @param {Boolean}
		 *            ignorePreceedingWS True to ignore the initial whitespaces
		 *            encountered
		 * @param {Node}
		 *            targetNode The node at which the search should reside
		 *            within.
		 * @param {Boolean}
		 *            seenTargetNode Flag as true if the target node has been
		 *            visited.
		 * @param {Boolean}
		 *            includeNBSP True to include non breaking spaces as
		 *            whitespace, false to only count whitespace.
		 * @param {Boolean}
		 *            ignoreInternalSingleWS True to ignore any single
		 *            whitespace sequences that are definatly breaking two words
		 *            apart
		 * @param {Boolean}
		 *            extendRange True to allow the sequences to go past the
		 *            target node for boundry cases.
		 * 
		 * @return {Object} An object with the following members: seenTargetNode -
		 *         true if the target node was encountered. resumeNode - The
		 *         node to resume the search for remaining ws sequences in the
		 *         target node resumeIndex - The index to resume the search for
		 *         remaining ws sequences in the target node startNode - The
		 *         start node of the sequence. Null if there was none.
		 *         startIndex - The start index of the sequence (if there was
		 *         one) endNode - The end node of the sequence, if there was
		 *         one. endIndex - The end index of the sequence, if there was
		 *         one.
		 */
		function nextWSSequence(initNode, initIndex, ignorePreceedingWS,
				targetNode, seenTargetNode, includeNBSP,
				ignoreInternalSingleWS, extendRange) {

			var resumeNode = null, resumeIndex = initIndex, startNode, startIndex, endNode, endIndex, startWSStyle, curWSStyle;

			// Locate the next whitespace sequence from the current node onwards
			// (if any).
			_visitAllNodes(
					null,
					initNode,
					true,
					function(domNode) {

						// Has the search space exhausted? I.E: Has the
						// traversal gone past the target node's descendants -
						// and at this point isn't looking for any whitespace to
						// consolidate?
						if (seenTargetNode && domNode != targetNode
								&& (!startNode || !extendRange)
								&& !_isAncestor(targetNode, domNode)) // Case:
																		// if
																		// target
																		// is
																		// text
																		// node,
																		// then
																		// it
																		// can
																		// split...
																		// and
																		// prematurely
																		// end
																		// search
							return false; // Finished consolidating/removing
											// ws

						// Update flag if domnode is the target node
						seenTargetNode |= (domNode == targetNode);

						// Set helper: the whitespace CSS style for the current
						// visited dom node
						curWSStyle = getWSStyle(domNode);

						if (startNode) {

							// If building a whitespace sequence, check to see
							// if the ancestors of the starting node - up to
							// the common ancestor of the start node and this
							// current node - can be contained in a whitespace
							// sequence.
							var ca = _getCommonAncestor(domNode, startNode,
									false);
							var ancestors = _getAncestors(startNode, ca, false,
									false);
							var terminateSeq = false;
							for ( var i in ancestors) {
								if (!(_isInlineLevel(ancestors[i]) && !nonWSInlineElements[_nodeName(ancestors[i])])) {
									terminateSeq = true;
									break;
								}
							}

							// Whitespace sequences cannot contain different
							// breaking mechanisms.
							if (terminateSeq || curWSStyle != startWSStyle) {
								resumeNode = domNode;
								resumeIndex = 0;
								return false;
							}
						}

						if (domNode.nodeType == Node.TEXT_NODE) {

							if (domNode.parentNode.nodeType != Node.COMMENT_NODE) {

								if (!_doesTextSupportNonWS(domNode)) {

									debug.assert(!ignorePreceedingWS);

									// If there is potentially something to
									// consolidate, abort this traversal
									if (startNode) {
										// Record current position to resume
										// traversal after consolidation
										resumeNode = domNode;
										resumeIndex = 0;
										return false;
									}

								} else {

									// Iterate over charactors in the text run
									while (resumeIndex < _nodeLength(domNode)) {
										var ch = domNode.nodeValue
												.charAt(resumeIndex);
										if (_isAllWhiteSpace(ch)
												|| (includeNBSP && ch == _NBSP)) {

											if (!ignorePreceedingWS) {

												// Note start/end node/index of
												// whitespace sequence
												if (startNode) {
													endNode = domNode;
													endIndex = resumeIndex + 1;
												} else {
													startNode = domNode;
													startWSStyle = curWSStyle;
													startIndex = resumeIndex;
													endNode = null;
												}
											}

										} else { // Non whitespace charactor
											ignorePreceedingWS = false;

											// Is there a current sequence that
											// has more than 1 whitespace, or
											// one that resides at the start of
											// this text run?
											if (endNode
													|| (startNode && (ignoreInternalSingleWS || startIndex == 0))) {

												// Record current position to
												// resume traversal after
												// consolidation
												resumeNode = domNode;
												return false;

												// Ignore any previous
												// single-whitespace sequences,
												// that do not reside at the
												// start of the text run
											} else
												startNode = null;
										}

										resumeIndex++;

									} // End loop: iterating over charactors
										// in text run
								}

							} else
								ignorePreceedingWS = false;

						} else { // Not a text node

							ignorePreceedingWS = false;

							if (domNode.nodeType != Node.COMMENT_NODE) {
								// Whitespace sequences can contain a subset of
								// inline elements.
								if (startNode
										&& !(_isInlineLevel(domNode) && !nonWSInlineElements[_nodeName(domNode)])) {
									resumeNode = domNode;
									resumeIndex = 0;
									return false;
								}
							}

							// The element at this point can be part of the
							// current whitespace sequence...

						}

						resumeIndex = 0;

					}); // End visit

			// If sequence is one in length, must set end position
			if (startNode && !endNode) {
				endNode = startNode;
				endIndex = startIndex + 1;
			}

			return {
				seenTargetNode : seenTargetNode,
				resumeNode : resumeNode,
				resumeIndex : resumeIndex,
				startNode : startNode,
				startIndex : startIndex,
				endNode : endNode,
				endIndex : endIndex
			};

		}

		/**
		 * @param {Node}
		 *            startNode The starting text node of the whitespace
		 *            sequence
		 * @param {Number}
		 *            startIndex The starting index of the whitespace sequence
		 * @param {Node}
		 *            endNode The ending text node of the whitespace sequence
		 * @param {Number}
		 *            endIndex The ending index of the whitespace sequence
		 * @return {Boolean} True iff the given whitespace sequence breaks two
		 *         words/breakable-elements apart.
		 */
		function isBreaker(startNode, startIndex, endNode, endIndex) {

			var startWSStyle = getWSStyle(startNode);

			// Look to the left
			if (startIndex == 0) {
				var found = false;
				_visitAllNodes(null, startNode, false,
						function(domNode) {

							if (domNode == startNode)
								return; // Skip initial text node

							var res = scan(domNode, startNode);

							if (!res && found) {
								// Check that all ancestors up to and excluding
								// the common ancestor of this dom node
								// and the start node, are all nodes which are
								// breaked by whitespace
								var ca = _getCommonAncestor(domNode, startNode,
										false);
								var ancestors = _getAncestors(domNode, ca,
										false, false);

								for ( var i in ancestors) {
									// Reset found flag
									found = false;

									// Check ancestor if it does not break on
									// whitespace...
									if (!scan(ancestors[i], startNode)
											&& !found)
										return false;
								}

								// Restore flags
								res = false;
								found = true;

							}
							return res;
						});

				if (!found)
					return false;
			}

			// Look to the right
			if (endIndex == _nodeLength(endNode)) {
				var found = false;
				_visitAllNodes(null, endNode, true,
						function(domNode) {
							if (domNode == endNode)
								return; // Skip initial text node

							var res = scan(domNode, endNode);

							if (!res && found) {
								// Check that all ancestors up to and excluding
								// the common ancestor of this dom node
								// and the end node, are all nodes which are
								// breakable by whitespace
								var ca = _getCommonAncestor(domNode, endNode,
										false);
								var ancestors = _getAncestors(endNode, ca,
										false, false);

								for ( var i in ancestors) {
									// Reset found flag
									found = false;

									// Check ancestor if it does not break on
									// whitespace...
									if (!scan(ancestors[i], endNode) && !found)
										return false;
								}
								// Restore flags
								found = true;
								res = false;

							}

							return res;
						});

				if (!found)
					return false;
			}

			return true;

			/**
			 * Inner helper function.
			 * 
			 * Sets the "found" local to true if domnode is considered breakable
			 * (in it's context)
			 * 
			 * @param {Node}
			 *            domNode The node to check
			 * @param {Node}
			 *            initialNode The end or start node of the scan
			 * @return {Boolean} True to continue scanning, false to abort...a
			 *         result was found.
			 */
			function scan(domNode, initialNode) {

				if (domNode.nodeType == Node.TEXT_NODE) {
					if (_nodeLength(domNode) > 0) {
						found = _doesTextSupportNonWS(domNode); // Non-WS nodes
																// are not
																// breakable
						return false;
					}

				} else if (breakableElements[_nodeName(domNode)]) {
					found = !_isAncestor(domNode, initialNode); // WS Doesn't
																// break from
																// within
																// breakable
																// nodes to
																// outside of
																// them
					return false;

					// If hit a block level element or line break before a
					// breakable node, then the sequence must be
					// leading or trailing text.
				} else if (_isBlockLevel(domNode) || _nodeName(domNode) == "br")
					return false;

				// Keep looking...
				return true;
			}// End inner scan

		}

		/**
		 * @param {Node}
		 *            node A node to get it's whitespace CSS style for.
		 * @return {String} The CSS white-space style for the given node, never
		 *         null/always is a style.
		 */
		function getWSStyle(node) {

			var style = _getComputedStyle(node, "white-space");

			if (!style) {

				// Check if descends from PRE
				do {
					if (_nodeName(node) == "pre") {
						style = "pre";
						break;
					}
					node = node.parentNode;
				} while (node && node.nodeType == Node.ELEMENT_NODE);

				// Set as normal
				if (!style)
					style = "normal";
			}

			return style;

		}

	})()
})();
