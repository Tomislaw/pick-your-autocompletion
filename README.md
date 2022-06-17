<img align="left" width="80" height="80" src="https://user-images.githubusercontent.com/29047748/173244657-997e41d3-1817-42d0-a470-8b879daf350c.svg" alt="Resume application project app icon">

# Pick Your Autocompletion

![Build](https://github.com/Tomislaw/pick-your-autocompletion/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)


<!-- Plugin description -->
The main idea behing Pick Your Autocompletion plugin is to provide free and open source alternative for AI powered auto completion assistants such as [Github Copilot](https://copilot.github.com/) or [Tabnine](https://www.tabnine.com/). 

Connect it with one of many AI providers like [OpenAi](https://openai.com/) or [HuggingFace](https://huggingface.co/).

Use your own custom hosted AI text transformer if you value privacy or want to develop open source AI projects.

<!-- Plugin description end -->

![Animation](https://user-images.githubusercontent.com/29047748/173250430-ef2c5d86-5776-4e64-b292-7a6f95a2555b.gif)


## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "pick-your-autocompletion"</kbd> >
  <kbd>Install Plugin</kbd>
  > :warning: **Not supported yet**
- Manually:

  Download the [latest release](https://github.com/Tomislaw/pick-your-autocompletion/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Instant configuration

To start using this plugin firstly you need to specify backend.

If you have access to GPT-3 api provided by [OpenAi](https://openai.com/) then you can use one click integration. You only need to input your api key.

You can also create account on [HuggingFace](https://huggingface.co/) and use one of theirs transformers. You willalso need api key if you want to use this provider.

![instant-integration](https://user-images.githubusercontent.com/29047748/173246468-b9f8c5b9-aced-4b97-b657-5a39550c251d.png)


## Custom configuration

Pick Your Autocompletion plugin allows you to use your custom backend for your autocompletion. If you have access to custom text transformer you can configure custom http request which will be used for fetching data needed for autocompletion.

![request-builder](https://user-images.githubusercontent.com/29047748/173246473-c587061f-c6c1-4aed-8d14-247606ab0dbe.png)

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
