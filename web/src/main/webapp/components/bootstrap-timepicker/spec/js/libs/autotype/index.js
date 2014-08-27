/**
 * jQuery.autotype - Simple, accurate, typing simulation for jQuery
 *
 * version 0.5.0
 * 
 * http://michaelmonteleone.net/projects/autotype
 * http://github.com/mmonteleone/jquery.autotype
 *
 * Copyright (c) 2009 Michael Monteleone
 * Licensed under terms of the MIT License (README.markdown)
 */
(function($){
    
    // code type constants
    var CHARACTER = 1,
        NON_CHARACTER = 2,
        MODIFIER_BEGIN = 3,
        MODIFIER_END = 4,
        isNullOrEmpty = function(val) { return val === null || val.length === 0; },
        isUpper = function(char) { return char.toUpperCase() === char; },
        isLower = function(char) { return char.toLowerCase() === char; },
        areDifferentlyCased = function(char1,char2) {
                return (isUpper(char1) && isLower(char2)) ||
                    (isLower(char1) && isUpper(char2));
            },
        convertCase = function(char) {
                return isUpper(char) ? char.toLowerCase() : char.toUpperCase();
            },
        parseCodes = function(value, codeMap) {
                // buffer to hold a collection of key/char code pairs corresponding to input string value
            var codes = [],
                // buffer to hold the name of a control key as it's being parsed
                definingControlKey = false,
                // hold a collection of currently pushed modifier keys
                activeModifiers = {
                    alt: false,
                    meta: false,
                    shift: false,
                    ctrl: false
                },
                explicitModifiers = $.extend({}, activeModifiers),
                // buffer to hold construction of current control key
                currentControlKey = '',
                previousChar = '', 
                pushCode = function(opts) {
                    codes.push($.extend({}, opts, activeModifiers));
                },
                pushModifierBeginCode = function(modifierName) {
                    activeModifiers[modifierName] = true;           
                    pushCode({
                        keyCode: codeMap[modifierName],
                        charCode: 0,
                        char: '',
                        type: MODIFIER_BEGIN                        
                    });     
                },
                pushModifierEndCode = function(modifierName) {
                    activeModifiers[modifierName] = false;
                    pushCode({
                        keyCode: codeMap[modifierName],
                        charCode: 0,
                        char: '',
                        type: MODIFIER_END
                    });
                };
            
            for(var i=0;i<value.length;i++) {
                // if the character is about to define a control key
                if(!definingControlKey && 
                    i <= value.length - 5 && 
                    value.charAt(i) === '{' && 
                    value.charAt(i+1) === '{') 
                {
                    // skip the next "{"                    
                    i++;
                    
                    definingControlKey = true;
                } 
                // if the character is about to end definition of control key
                else if (definingControlKey && 
                    i <= value.length - 2 &&
                    value.charAt(i) === '}' &&
                    value.charAt(i+1) === '}')
                {
                    // skip the next "}"
                    i++;
                
                    // check if this key is a modifier-opener (is a ctrl,alt,del,shift)
                    if(activeModifiers[currentControlKey] !== undefined) 
                    {
                        explicitModifiers[currentControlKey] = true;
                        pushModifierBeginCode(currentControlKey);
                    } 
                    // check if this key is a modifier-closer (is a /ctrl,/alt,/del,.shift)                
                    else if(activeModifiers[currentControlKey.substring(1)] !== undefined) 
                    {
                        explicitModifiers[currentControlKey] = false;
                        pushModifierEndCode(currentControlKey.substring(1));
                    } 
                    // otherwise is some other kind of non-modifier control key
                    else 
                    {
                        pushCode({
                            keyCode: codeMap[currentControlKey],
                            charCode: 0,
                            char: '',
                            type: NON_CHARACTER,
                            controlKeyName: currentControlKey
                        });
                    }

                    definingControlKey = false;
                    currentControlKey = '';
                }
                // currently defining control key
                else if (definingControlKey) 
                {
                    currentControlKey += value.charAt(i);
                } 
                // otherwise is just a text character
                else 
                {
                    var character = value.charAt(i);
                    
                    // check for any implicitly changing of cases, and register presses/releases
                    // of the shift key in accord with them.
                    if(
                        (!isNullOrEmpty(previousChar) && areDifferentlyCased(previousChar, character)) ||
                        (isNullOrEmpty(previousChar) && isUpper(character))
                      )
                    {
                        if(isUpper(character) && !activeModifiers.shift) {
                            pushModifierBeginCode("shift");
                        } else if (isLower(character) && activeModifiers.shift && !explicitModifiers.shift){
                            pushModifierEndCode("shift");
                        }
                    }
                    
                    // modify the current character if there are active modifiers
                    if((activeModifiers.shift && isLower(character)) || 
                        (!activeModifiers.shift && isUpper(character))) {
                        // shift converts case
                        character = convertCase(character);
                    }
                    
                    var code = {
                        // if can't identify a keycode, just fudge with the char code.
                        // nope, this isn't ideal by any means.
                        keyCode: codeMap[character] || character.charCodeAt(0),
                        charCode: character.charCodeAt(0),
                        char: character,
                        type: CHARACTER
                    };
                    
                    // modify the current character if there are active modifiers
                    if(activeModifiers.alt || 
                        activeModifiers.ctrl ||
                        activeModifiers.meta) {
                        // alt, ctrl, meta make it so nothing is typed
                        code.char = '';
                    }
                    pushCode(code); 
                    if(code.char !== '') { previousChar = code.char; }
                }
            }
            return codes;        
        },    
        triggerCodeOnField = function(code, field) {
            // build up base content that every event should contain
            // with information about whether certain chord keys are 
            // simulated as being pressed
            var evnt = {
                altKey: code.alt,
                metaKey: code.meta,
                shiftKey: code.shift,
                ctrlKey: code.ctrl
            };

            // build out 3 event instances for all the steps of a key entry
            var keyDownEvent = $.extend($.Event(), evnt, {type:'keydown', keyCode: code.keyCode, charCode: 0, which: code.keyCode});
            var keyPressEvent = $.extend($.Event(), evnt, {type:'keypress', keyCode: 0, charCode: code.charCode, which: code.charCode || code.keyCode});
            var keyUpEvent = $.extend($.Event(), evnt, {type:'keyup', keyCode: code.keyCode, charCode: 0, which: code.keyCode});
        
            // go ahead and trigger the first 2 (down and press)         
            // a keyup of a modifier shouldn't also re-trigger a keydown       
            if(code.type !== MODIFIER_END) {
                field.trigger(keyDownEvent);                    
            }
            
            // modifier keys don't have a keypress event, only down or up
            if(code.type !== MODIFIER_BEGIN && code.type !== MODIFIER_END) {
                field.trigger(keyPressEvent);
            }
            
            // only actually add the new character to the input if the keydown or keypress events 
            // weren't cancelled by any consuming event handlers
            if(!keyDownEvent.isPropagationStopped() && 
                !keyPressEvent.isPropagationStopped()) {
                if(code.type === NON_CHARACTER) {
                    switch(code.controlKeyName) {
                        case 'enter':
                            field.val(field.val() + "\n");
                            break;
                        case 'back':
                            field.val(field.val().substring(0,field.val().length-1));
                            break;
                    }
                } else {
                    field.val(field.val() + code.char);                    
                }
            }
        
            // then also trigger the 3rd event (up)
            // a keydown of a modifier shouldn't also trigger a keyup until coded
            if(code.type !== MODIFIER_BEGIN) {
                field.trigger(keyUpEvent);                    
            }                        
        },
        triggerCodesOnField = function(codes, field, delay, global) {
            if(delay > 0) {
                codes = codes.reverse();
                var keyInterval = global.setInterval(function(){
                    var code = codes.pop();
                    triggerCodeOnField(code, field);
                    if(codes.length === 0) {
                        global.clearInterval(keyInterval);
                        field.trigger('autotyped');                            
                    }
                }, delay);                
            } else {
                $.each(codes,function(){                    
                    triggerCodeOnField(this, field);
                });
                field.trigger('autotyped');
            }                
        };
    
    $.fn.autotype = function(value, options) {
        if(value === undefined || value === null) { throw("Value is required by jQuery.autotype plugin"); }
        var settings = $.extend({}, $.fn.autotype.defaults, options);        

        // 1st Pass        
        // step through the input string and convert it into 
        // a logical sequence of steps, key, and charcodes to apply to the inputs
        var codes = parseCodes(value, settings.keyCodes[settings.keyBoard]);

        // 2nd Pass
        // Run the translated codes against each input through a realistic
        // and cancelable series of key down/press/up events        
        return this.each(function(){ triggerCodesOnField(codes, $(this), settings.delay, settings.global); });
    };
    
    $.fn.autotype.defaults = {
        version: '0.5.0',
        keyBoard: 'enUs',
        delay: 0,
        global: window,
        keyCodes: {
            enUs: { 'back':8,'ins':45,'del':46,'enter':13,'shift':16,'ctrl':17,'meta':224,
                'alt':18,'pause':19,'caps':20,'esc':27,'pgup':33,'pgdn':34,
                'end':35,'home':36,'left':37,'up':38,'right':39,'down':40,
                'printscr':44,'num0':96,'num1':97,'num2':98,'num3':99,'num4':100,
                'num5':101,'num6':102,'num7':103,'num8':104,'num9':105,
                'multiply':106,'add':107,'subtract':109,'decimal':110,
                'divide':111,'f1':112,'f2':113,'f3':114,'f4':115,'f5':116,
                'f6':117,'f7':118,'f8':119,'f9':120,'f10':121,'f11':122,
                'f12':123,'numlock':144,'scrolllock':145,'   ':9,' ':32,
                'tab':9,'space':32,'0':48,'1':49,'2':50,'3':51,'4':52,
                '5':53,'6':54,'7':55,'8':56,'9':57,')':48,'!':49,'@':50,
                '#':51,'$':52,'%':53,'^':54,'&':55,'*':56,'(':57,';':186,
                '=':187,',':188,'-':189,'.':190,'/':191,'[':219,'\\':220,
                ']':221,"'":222,':':186,'+':187,'<':188,'_':189,'>':190,
                '?':191,'{':219,'|':220,'}':221,'"':222,'a':65,'b':66,'c':67,
                'd':68,'e':69,'f':70,'g':71,'h':72,'i':73,'j':74,'k':75,
                'l':76,'m':77,'n':78,'o':79,'p':80,'q':81,'r':82,'s':83,
                't':84,'u':85,'v':86,'w':87,'x':88,'y':89,'z':90,'A':65,
                'B':66,'C':67,'D':68,'E':69,'F':70,'G':71,'H':72,'I':73,
                'J':74,'K':75,'L':76,'M':77,'N':78,'O':79,'P':80,'Q':81,
                'R':82,'S':83,'T':84,'U':85,'V':86,'W':87,'X':88,'Y':89,'Z':90 }            
        }        
    };
    
})(jQuery);
