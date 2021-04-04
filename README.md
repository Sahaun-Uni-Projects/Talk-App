<h1 align="center">Talk App</h1>
<p align="center">No, this is not Discord</p>

---

Talk App is a minimal chat application with basic features including:
- Signing up and logging in
- Updating user profile
- Managing friends and friend requests
- Active status of friends
- Real-time chat
- Database integration

&nbsp;

![Main](https://user-images.githubusercontent.com/27750907/113517286-c5287100-95a0-11eb-8850-bf59606b8157.png)
<p align="center"><i>Chat with friends</i></p>

&nbsp;

![Friends](https://user-images.githubusercontent.com/27750907/113517434-c312e200-95a1-11eb-9770-e82fab7f70ad.png)
<p align="center"><i>Unfriend 'em!</i></p>

&nbsp;

Software used:
- <a href="https://netbeans.apache.org/">Apache NetBeans 12.2</a> (IDE)
- <a href="https://gluonhq.com/products/scene-builder/">Scene Builder</a> (For .fxml files)
- <a href="https://inkscape.org/">Inkscape</a> (For the colorful pictures)

&nbsp;

Libraries used:
- <a href="https://dev.mysql.com/downloads/connector/j/">MySQL Connector Java 8.0.23</a>
- <a href="https://code.google.com/archive/p/json-simple/">JSON Simple 1.1</a>
- <a href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">Java SE Development Kit 8u281</a>

&nbsp;

To use the program, you need to:
- Have an active database named `talk_db` (the name can be changed in <a href="https://github.com/sohomsahaun/Talk-App/blob/main/TalkApp/src/database/Database.java#L26">Database.java</a>)
- Run <a href="https://github.com/sohomsahaun/Talk-App/blob/main/TalkApp/src/server/ServerMain.java">ServerMain.java</a>
- Run <a href="https://github.com/sohomsahaun/Talk-App/blob/main/TalkApp/src/client/ClientMain.java">ClientMain.java</a> (for each window)

You can change the server name and port in <a href="https://github.com/sohomsahaun/Talk-App/blob/6927dc7e53fdf5162a48cec874e9a702bcc38c84/TalkApp/src/utils/MACRO.java">MACRO.java</a>

&nbsp;

---


> What works, works!

Note: This project has been made as a part of a software development course in university. The code is very unoptimized and probably looks like a game jam code, as all university projects should be.
