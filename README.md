# Trainer Workload Microservice

Este microservicio gestiona la carga de trabajo de entrenadores, incluyendo la adición/eliminación de sesiones de entrenamiento y la consulta de resúmenes mensuales o anuales por entrenador.

## 📦 Tecnologías

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database (solo desarrollo)
- Maven
## 📦 Endpoints

### 1. **Actualizar carga de trabajo**

Registra una nueva sesión de entrenamiento o elimina una previamente registrada, según el tipo de acción.

- **POST** `/api/trainer-workload/update`

#### 🔸 Body (JSON)
```json
{
  "username": "trainer01",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "trainingDate": "2025-07-20",
  "trainingDuration": 60,
  "actionType": "ADD" or "DELETE" 
}

```

### 2. Consultar resumen mensual de un entrenador

Obtiene el resumen de actividad mensual de un entrenador, con filtros opcionales por año y mes.

**GET** `/api/trainer-workload/summary/{username}`

##### 🔸 Parámetros de consulta (opcionales)
- year: año (e.g., 2025)
- month: mes (1-12)

```
curl -X GET "http://localhost:8081/api/trainer-workload/summary/trainer01?year=2025&month=7"

```


##### 📤 Respuesta (JSON)
```json
{
    "username": "juan123",
    "firstName": "Juan",
    "lastName": "Pérez",
    "status": true,
    "years": [
        {
            "year": 2025,
            "months": [
                {
                    "month": 7,
                    "trainingSummaryDuration": 90
                },
                {
                    "month": 3,
                    "trainingSummaryDuration": 90
                }
            ]
        },
        {
            "year": 2024,
            "months": [
                {
                    "month": 6,
                    "trainingSummaryDuration": 90
                }
            ]
        }
    ]
}
```

### 3. 📁 Estructura
```
trainer-workload-service/
├── controller/
│   └── TrainerWorkloadController.java
├── service/
│   └── TrainerWorkloadService.java
├── model/
│   └── TrainerSummary.java
├── repository/
│   └── TrainerSummaryRepository.java
└── GymAppApplication.java
```

### 4. 🧾 Logs

**transactionLogger**: para registrar las transacciones con ID único por solicitud.
**operationLogger**: para registrar operaciones internas específicas como lógica de negocio.

