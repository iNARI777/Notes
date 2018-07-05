# Web 相关的基础知识

## 1. Cookie 基本知识

Cookie 是一段存放在 **浏览器** 中，用于存放 **用户** 相关信息的一段数据。而浏览器是根据 **服务器** 的 **响应头** 的指示来记录 Cookie 的。

当用户发送请求给同一个 **域** 的时候，浏览器会自动将 Cookie 添加到请求的请求头中发送给服务器。Cookie 在请求头中的格式是

  cookie:key1=value1; key2=value2; key3=value3

可见， Cookie 可以用作一个存储工具，但是为了避免 cookie 的滥用，大多浏览器厂商都会对 cookie 的大小和数量作出限制，比如每个 cookie 大小最大为 4KB，每个域下的 cookie 最多可以有 20 个等。

**Cookie 的发送**

每次浏览器在发送 http 请求的时候都会检查一下在这个域中是否有相应的 cookie，如果有相应的 cookie，浏览器就要将其放入 `request header` 中。

**Cookie 的添加**

Cookie 的添加可以在服务端的 **响应头** 中的 `set-Cookie` 字段设置，每个 `set-Cookie` 可以设置一个 cookie。

Cookie 的设置也可以在浏览器端通过 JavaScript 进行设置，。

**Cookie 的属性**

每个 cookie 除了存储的内容之外，浏览器还会存储每一个 cookie 的属性。

* expires ：用来设置有效时间
* domain+path ：共同构成了 URL，这个 URL 用来表示一个 Cookie 能够被哪些 URL 访问。 domain 的默认值为这个 cookie 所在的域名，而 path 的默认值为设置该 cookie 的页面所在的路径。
* httpOnly ：用来设置该 cookie 只能被 http 请求访问，而 JavaScript 等其他方法无法访问该 cookie。
* secure ：这个配置要求服务器支持 https 协议，因为这个配置是用来将 cookie 加密用的。
