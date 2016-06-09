# Executive Chat

Executive Chat is an application that allows you to chat with someone (given their IP address) over a secure encrypted connection.

## Building

Import the project into Eclipse, modify the code however you want, and build.

## Technical Information

### Chat message encryption

Chat messages are encrypted using RSA-4096 and AES-256 and sent over a TCP connection to the intended recipient's IP address. The message is then decrypted by the recipient using their private key.

### Key format

RSA keys are stored on disk using the X509 and PKCS8 key formats.

