<template>
    <div class="simple-keyboard"></div>
  </template>
  
  <script setup>
  import { onMounted } from 'vue'
  import { SimpleKeyboard } from 'simple-keyboard'
  import 'simple-keyboard/build/css/index.css'
  
  const emit = defineEmits(['key'])
  
  onMounted(() => {
    let layoutName = 'default'
  
    const keyboard = new SimpleKeyboard('.simple-keyboard', {
      layoutName,
  
      layout: {
        default: [
          '1 2 3 4 5 6 7 8 9 0',
          'q w e r t y u i o p',
          'a s d f g h j k l',
          '{shift} z x c v b n m',
          '! @ # $ % ^ & *',
          '{bksp}'
        ],
  
        shift: [
          '1 2 3 4 5 6 7 8 9 0',
          'Q W E R T Y U I O P',
          'A S D F G H J K L',
          '{shift} Z X C V B N M',
          '! @ # $ % ^ & *',
          '{bksp}'
        ]
      },
  
      display: {
        '{shift}': '⇧',
        '{bksp}': '⌫'
     
      },
  
      onKeyPress: (button) => {
        if (button === '{shift}') {
          layoutName = layoutName === 'default'
            ? 'shift'
            : 'default'
  
          keyboard.setOptions({
            layoutName
          })
          return
        }
  
        emit('key', button)
      }
    })
  })
  </script>