<script setup lang="ts">
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const schema = toTypedSchema(
  z.object({
    email: z.string().min(1, '이메일을 입력하세요').email('올바른 이메일 형식이 아닙니다'),
    password: z.string().min(1, '비밀번호를 입력하세요')
  })
)

const { handleSubmit, errors } = useForm({ validationSchema: schema })
const { value: email } = useField<string>('email')
const { value: password } = useField<string>('password')

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const isLoading = ref(false)
const serverError = ref('')

const onSubmit = handleSubmit(async (values) => {
  isLoading.value = true
  serverError.value = ''
  try {
    await authStore.login(values)
    const redirect = route.query.redirect as string
    router.push(redirect || '/dashboard')
  } catch (err: any) {
    serverError.value = err.response?.data?.error?.detail ?? '로그인에 실패했습니다'
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <form @submit.prevent="onSubmit" class="space-y-4">
    <BaseInput
      v-model="email"
      label="이메일"
      type="email"
      placeholder="you@example.com"
      :error="errors.email"
      required
    />
    <BaseInput
      v-model="password"
      label="비밀번호"
      type="password"
      placeholder="••••••••"
      :error="errors.password"
      required
    />

    <p v-if="serverError" class="text-sm text-red-600">{{ serverError }}</p>

    <BaseButton type="submit" :loading="isLoading" class="w-full">
      로그인
    </BaseButton>
  </form>
</template>
