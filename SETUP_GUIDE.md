# 📱 COMANDA DIGITAL - APP DO GARÇOM

**Documentação Completa de Setup e Desenvolvimento**

---

## 🎯 Visão Geral

Comanda Digital é um aplicativo Android completo para gerenciamento de atendimentos em restaurantes. Desenvolvedores, garçons utilizam este app para:

- ✅ Gerenciar mesas em tempo real
- ✅ Registrar pedidos com adicionais
- ✅ Acompanhar status de preparação
- ✅ Processar pagamentos
- ✅ Imprimir comandas e recibos
- ✅ Unir mesas para atendimento conjunto

---

## 📋 Pré-requisitos

### Sistema
- **Java 17+**
- **Android Studio 2023.1.1+**
- **Gradle 8.0+**
- **Android SDK 34+**
- **Min SDK: 24** (Android 7.0)

### Serviços
- **Firebase Project** configurado
- **Google Cloud Console** acesso
- **Firestore Database** criado
- **Firebase Authentication** habilitado

---

## 🚀 Setup Inicial

### 1. Clonar Repositório

```bash
git clone https://github.com/marinilanches/app.git
cd app
```

### 2. Configurar Firebase

#### 2.1 Criar Projeto Firebase

1. Acesse [Firebase Console](https://console.firebase.google.com)
2. Clique em "Criar Projeto"
3. Nome: "Mesa Fácil"
4. Desabilite Google Analytics
5. Crie o projeto

#### 2.2 Adicionar App Android

1. No projeto Firebase, clique em "Adicionar App"
2. Selecione Android
3. **Package Name**: `com.example.mesafacil`
4. Baixe o arquivo `google-services.json`
5. Coloque em: `app/google-services.json`

#### 2.3 Configurar Authentication

1. Vá em **Authentication** > **Sign-in method**
2. Habilite **Email/Password**
3. Habilite **Anonymous** (opcional)

#### 2.4 Criar Firestore Database

1. Vá em **Firestore Database**
2. Clique "Criar banco de dados"
3. Selecione região: `us-central1`
4. Modo: **Teste** (depois mude para Produção)
5. Crie o banco

#### 2.5 Configurar Regras de Segurança

Vá em **Firestore** > **Regras** e coloque:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuários autenticados podem ler/escrever seus dados
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    
    // Mesas
    match /mesas/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Pedidos
    match /pedidos/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Pagamentos
    match /pagamentos/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Menu
    match /menu/{document=**} {
      allow read: if request.auth != null;
    }
    
    // Adicionais
    match /adicionais/{document=**} {
      allow read: if request.auth != null;
    }
  }
}
```

### 3. Abrir Projeto no Android Studio

```bash
open -a "Android Studio" .
```

### 4. Build do Projeto

```bash
./gradlew build
```

### 5. Executar no Emulador

```bash
./gradlew installDebug
```

---

## 📁 Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/example/mesafacil/
│   │   ├── MainActivity.kt                    # Activity principal
│   │   ├── ui/
│   │   │   ├── screens/                       # Telas Compose
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── MesasScreen.kt
│   │   │   │   ├── PedidoScreen.kt
│   │   │   │   ├── StatusPedidosScreen.kt
│   │   │   │   ├── PagamentoScreen.kt
│   │   │   │   └── OpcoesMesaScreen.kt
│   │   │   ├── viewmodels/                    # ViewModels
│   │   │   │   ├── AuthViewModel.kt
│   │   │   │   ├── MesaViewModel.kt
│   │   │   │   ├── PedidoViewModel.kt
│   │   │   │   └── PagamentoViewModel.kt
│   │   │   ├── theme/
│   │   │   │   └── Theme.kt
│   │   │   └── navigation/
│   │   │       └── Navigation.kt
│   │   ├── data/
│   │   │   ├── models/                        # Data Models
│   │   │   │   ├── User.kt
│   │   │   │   ├── Mesa.kt
│   │   │   │   ├── Pedido.kt
│   │   │   │   ├── Pagamento.kt
│   │   │   │   └── MenuItem.kt
│   │   │   └── repositories/                  # Firebase Repositories
│   │   │       ├── AuthRepository.kt
│   │   │       ├── MesaRepository.kt
│   │   │       ├── PedidoRepository.kt
│   │   │       ├── PagamentoRepository.kt
│   │   │       └── MenuRepository.kt
│   │   └── utils/
│   │       ├── FirebaseInitializer.kt
│   │       ├── PrinterManager.kt
│   │       ├── LoggerUtil.kt
│   │       └── Extensions.kt
│   ├── res/
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   ├── colors.xml
│   │   │   └── themes.xml
│   │   ├── xml/
│   │   │   ├── data_extraction_rules.xml
│   │   │   └── backup_rules.xml
│   │   └── mipmap/
│   │       └── ic_launcher.xml
│   └── AndroidManifest.xml
├── build.gradle.kts                         # Dependências
└── google-services.json                     # Credenciais Firebase
```

---

## 🏗️ Arquitetura

### Padrão MVVM + Clean Architecture

```
┌─────────────────────────────────────────┐
│         UI Layer (Jetpack Compose)      │
│  - Screens                              │
│  - Components                           │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│       Presentation Layer (ViewModels)   │
│  - State Management                     │
│  - Business Logic                       │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│       Domain Layer (Use Cases)          │
│  - Repository Interfaces                │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│       Data Layer (Firebase)             │
│  - Firestore Database                   │
│  - Authentication                       │
│  - Repositories                         │
└─────────────────────────────────────────┘
```

---

## 🔑 Fluxo de Dados

### Login

```
LoginScreen
    ↓
AuthViewModel.login(email, password)
    ↓
AuthRepository.login()
    ↓
Firebase Authentication
    ↓
Carrega User do Firestore
    ↓
Atualiza authState
    ↓
Navega para MesasScreen
```

### Novo Pedido

```
PedidoScreen
    ↓
Seleciona itens do menu
    ↓
Adiciona adicionais
    ↓
Clica "Enviar"
    ↓
PedidoViewModel.criarPedido()
    ↓
PedidoRepository.createPedido()
    ↓
Salva em Firestore
    ↓
PrintManager.printComanda()
    ↓
Imprime no servidor de impressão
    ↓
AtualizaStatusPedidos em tempo real
```

### Pagamento

```
PagamentoScreen
    ↓
Digita valor e forma de pagamento
    ↓
Clica "Adicionar"
    ↓
PagamentoViewModel.adicionarPagamento()
    ↓
PagamentoRepository.adicionarPagamentoParcial()
    ↓
Atualiza no Firestore
    ↓
Se completo → PrintManager.printRecibo()
    ↓
Fechar Mesa
```

---

## 📊 Estrutura Firestore

### Collection: `users`

```json
{
  "userId": {
    "email": "garcom@mesafacil.com",
    "name": "João",
    "role": "waiter",
    "isActive": true,
    "createdAt": 1655000000000,
    "updatedAt": 1655000000000
  }
}
```

### Collection: `mesas`

```json
{
  "mesaId": {
    "numero": 1,
    "status": "LIVRE",
    "quantidadePessoas": 4,
    "valorTotal": 125.50,
    "garcomId": "userId",
    "garcomNome": "João",
    "mesasUnidas": [1, 2],
    "createdAt": 1655000000000,
    "updatedAt": 1655000000000
  }
}
```

### Collection: `pedidos`

```json
{
  "pedidoId": {
    "mesaId": "mesaId",
    "numeroMesa": 1,
    "itens": [
      {
        "id": "itemId",
        "nome": "X-Burger",
        "quantidade": 2,
        "valorUnitario": 25.00,
        "adicionais": [
          {
            "id": "adicionalId",
            "nome": "Bacon",
            "valor": 5.00
          }
        ],
        "observacoes": "Sem cebola"
      }
    ],
    "status": "NOVO",
    "valorTotal": 125.50,
    "observacoes": "Mesa 1",
    "garcomId": "userId",
    "garcomNome": "João",
    "createdAt": 1655000000000,
    "updatedAt": 1655000000000
  }
}
```

### Collection: `pagamentos`

```json
{
  "pagamentoId": {
    "mesaId": "mesaId",
    "numeroMesa": 1,
    "valorTotal": 125.50,
    "quantidadePessoas": 4,
    "quantidadePagantes": 2,
    "pagamentos": [
      {
        "valor": 65.00,
        "formaPagamento": "PIX",
        "timestamp": 1655000000000
      },
      {
        "valor": 60.50,
        "formaPagamento": "DINHEIRO",
        "timestamp": 1655000100000
      }
    ],
    "formaPagamento": "DINHEIRO",
    "status": "COMPLETO",
    "troco": 15.00,
    "garcomId": "userId",
    "createdAt": 1655000000000,
    "closedAt": 1655000200000
  }
}
```

### Collection: `menu`

```json
{
  "menuItemId": {
    "nome": "X-Burger",
    "categoria": "Lanches",
    "descricao": "Pão, hambúrguer, queijo e alface",
    "valor": 25.00,
    "disponivel": true,
    "imagem": "url_imagem",
    "adicionaisDisponiveis": ["adicionalId1", "adicionalId2"],
    "createdAt": 1655000000000
  }
}
```

### Collection: `adicionais`

```json
{
  "adicionalId": {
    "nome": "Bacon",
    "valor": 5.00,
    "disponivel": true,
    "createdAt": 1655000000000
  }
}
```

---

## 🔐 Autenticação

### Fluxo de Login

1. Usuário insere email e senha
2. `AuthViewModel.login()` chama `AuthRepository.login()`
3. Firebase Authentication autentica o usuário
4. Busca dados do usuário em Firestore
5. Armazena sessão localmente
6. Navega para tela de mesas

### Logout

1. Usuário clica em "Sair"
2. `AuthViewModel.logout()` chama `AuthRepository.logout()`
3. Firebase desconecta a sessão
4. Limpa dados locais
5. Retorna para tela de login

---

## 📱 Navegação

### Estados da Aplicação

```
┌──────────┐
│  Login   │ ← Usuário não autenticado
└────┬─────┘
     │ Login bem-sucedido
     ↓
┌──────────┐
│  Mesas   │ ← Lista todas as mesas
└────┬─────┘
     │ Clique em mesa
     ↓
┌──────────────────┐
│  Opções da Mesa  │ ← Novo pedido, Ver pedidos, Pagamento
└───┬──────┬───┬───┘
    │      │   │
    ↓      ↓   ↓
┌─────┐ ┌──────┐ ┌───────────┐
│Novo │ │Status│ │Pagamento  │
│Pedi │ │Pedido│ │           │
└─────┘ └──────┘ └───────────┘
```

---

## 🛠️ Desenvolvimento

### Adicionar Nova Tela

1. Criar arquivo `src/main/java/com/example/mesafacil/ui/screens/NovaScreen.kt`
2. Implementar composable
3. Adicionar ViewModel em `ui/viewmodels/NovaViewModel.kt`
4. Integrar em `MainActivity.kt`

### Adicionar Nova Funcionalidade Firebase

1. Criar model em `data/models/`
2. Criar repository em `data/repositories/`
3. Criar ViewModel em `ui/viewmodels/`
4. Integrar na tela correspondente

---

## 📝 Utilidades

### FirebaseInitializer

Inicializa Firebase na primeira execução:

```kotlin
FirebaseInitializer.initialize(context)
```

### PrinterManager

Gerencia impressão de comandas e recibos:

```kotlin
// Imprimir comanda
PrinterManager.printComanda(pedido)

// Imprimir recibo
PrinterManager.printRecibo(pagamento)
```

### Extensions

Funções utilitárias Kotlin:

```kotlin
// Formatar data
long.formatarData("dd/MM/yyyy HH:mm")
long.formatarHora()

// Formatar moeda
double.formatarMoeda() // "R$ 25,00"

// Validar email
string.isEmailValid()

// Toast
context.showToast("Mensagem")
```

### LoggerUtil

Logging centralizado:

```kotlin
LoggerUtil.d("Debug message")
LoggerUtil.e("Error message", exception)
LoggerUtil.i("Info message")
LoggerUtil.w("Warning message")
```

---

## 🧪 Testes

### Teste de Login

```kotlin
@Test
fun testLogin() = runBlocking {
    val result = authRepository.login("test@test.com", "password")
    assertTrue(result.isSuccess)
}
```

### Teste de Criação de Pedido

```kotlin
@Test
fun testCreatePedido() = runBlocking {
    val result = pedidoRepository.createPedido(
        mesaId = "1",
        numeroMesa = 1,
        itens = listOf(testItem),
        observacoes = "",
        garcomId = "user1",
        garcomNome = "João"
    )
    assertTrue(result.isSuccess)
}
```

---

## 🐛 Troubleshooting

### Problema: App não conecta ao Firebase

**Solução:**
1. Verifique `google-services.json` está em `app/`
2. Confirme package name: `com.example.mesafacil`
3. Recrie arquivo JSON no Firebase Console

### Problema: Erro de autenticação

**Solução:**
1. Verifique se email/password está habilitado em Firebase Auth
2. Crie usuário de teste no Firebase Console
3. Confirme regras de segurança do Firestore

### Problema: Dados não sincronizam

**Solução:**
1. Verifique conexão de internet
2. Confirme regras de leitura/escrita Firestore
3. Verifique logs em `LoggerUtil`

---

## 📦 Dependências Principais

- **Firebase BOM 33.1.2**
- **Jetpack Compose 2024.06.00**
- **Kotlin 1.9.24**
- **Android Material3 1.2.1**
- **Coroutines 1.8.1**

---

## 🚀 Próximos Passos

- [ ] Integrar com impressora Bluetooth
- [ ] Implementar relatórios
- [ ] Sincronização offline
- [ ] Notificações em tempo real
- [ ] Dashboard administrativo
- [ ] Suporte multi-idioma
- [ ] Testes automatizados
- [ ] CI/CD com GitHub Actions

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Verificar logs: `adb logcat`
2. Consultar Firebase Console
3. Revisar documentação oficial
4. Abrir issue no GitHub

---

**Desenvolvido com ❤️ por Marini Lanches**
