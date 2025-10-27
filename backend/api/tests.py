"""
api/tests.py
Pruebas mínimas (smoke) para comprobar que el API está de pie.
"""

from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status


class BasicFlowTests(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(username="ana", password="CLAVE_ANA")

    def test_jwt_and_crud(self):
        # Obtener tokens
        resp = self.client.post("/api/v1/cropcare/auth/jwt/create/", {"username": "ana", "password": "CLAVE_ANA"}, format="json")
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        access = resp.data["access"]

        # ping protegido
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {access}")
        resp = self.client.get("/api/v1/cropcare/ping/")
        self.assertEqual(resp.status_code, status.HTTP_200_OK)

        # crear plot
        payload = {"name": "Lote Norte", "cultivo": "tomate", "superficie_ha": 1.5, "fecha_siembra": "2025-09-01", "notes": "ensayo"}
        resp = self.client.post("/api/v1/cropcare/plots/", payload, format="json")
        self.assertEqual(resp.status_code, status.HTTP_201_CREATED)
