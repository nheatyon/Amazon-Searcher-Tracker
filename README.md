<h1 align="center">Amazon Searcher & Tracker (Telegram)</h1>

<b>An old project of mine recovered from a hard drive ☺️</b>

This simple Telegram bot will allow you to perform a variety of tasks among those mentioned in the screenshot, including performing a search for Amazon discount products and tracking them. Additional utilities are also present, which you can safely observe by inspecting the bot's code. For any modifications you can refer to the <b>“settings.json”</b> configuration file in order to customize the various messages and parameters.<br/>

## Getting started

<p align="center">
  <img src="https://i.imgur.com/85VVuNE.png" width="402" height="233"/>
  &nbsp;&nbsp;
  <img src="https://i.imgur.com/4mKssnt.png" width="402" height="233"/>
</p>

<mark><b>⚠️ Ehy! Some of the bot commands are written in the Italian language. To rename them, you will have to refer to the specific classes.</b></mark>

To make the bot work, you will initially have to change these parameters in the configuration:
```
"bot_token": "INSERT_YOUR_BOT_TOKEN",
...
"amazon_access_key": "INSERT_YOUR_AMAZON_ACCESS_KEY",
"amazon_secret_key": "INSERT_YOUR_AMAZON_SECRET_KEY",
"amazon_partner_tag": "INSERT_YOUR_AMAZON_PARTNER_TAG",
"amazon_host": "INSERT_YOUR_AMAZON_HOST",
"amazon_region": "INSERT_YOUR_AMAZON_REGION",
...
```
The data refer to your Amazon affiliate account (must be enabled to use PAAPI)

## Technologies used (What I remember)

* <a href="https://github.com/rubenlagus/TelegramBots">TelegramBots API</a>
* <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a>
* <a href="https://github.com/xerial/sqlite-jdbc">SQLite-JDBC</a>
* <a href="https://projectlombok.org">Lombok</a>
<br>
<i>And other things like <b>Amazon Product Advertising API</b> (check <b>pom.xml</b> for additional dependencies)</i>

## Contributing

Any contribution to the project is really <b>appreciated</b>. Feel free to fork the project and commit your changes!<br/>
